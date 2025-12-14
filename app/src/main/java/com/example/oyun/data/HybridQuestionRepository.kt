package com.example.oyun.data

import android.content.Context
import android.util.Log
import com.example.oyun.data.local.CachedQuestion
import com.example.oyun.data.local.CachedQuestionDao
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hybrid Question Repository
 * 
 * Strateji:
 * 1. Uygulama açılışında JSON'dan (assets) temel soruları yükle
 * 2. Arka planda Firestore'dan güncel soruları indir (level 20+)
 * 3. İndirilen soruları Room Database'e kaydet
 * 4. Oyun her zaman önce Room'dan, yoksa JSON'dan oku
 */
@Singleton
class HybridQuestionRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cachedQuestionDao: CachedQuestionDao,
    private val firestore: FirebaseFirestore,
    private val gson: Gson
) {
    
    companion object {
        private const val TAG = "HybridQuestionRepo"
        private const val FIRESTORE_SYNC_LEVEL = 20 // 20. seviyeden sonrası Firestore'dan
        private const val SYNC_INTERVAL_MS = 24 * 60 * 60 * 1000L // 24 saat
    }
    
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var jsonQuestions: List<Question> = emptyList()
    private var isInitialized = false
    
    // ============================================
    // INITIALIZATION
    // ============================================
    
    /**
     * Repository'yi başlatır
     * 1. JSON'dan temel soruları yükler
     * 2. Arka planda Firestore sync başlatır
     */
    suspend fun initialize() {
        if (isInitialized) return
        
        withContext(Dispatchers.IO) {
            try {
                // 1. JSON'dan yükle
                loadQuestionsFromJson()
                Log.d(TAG, "✅ JSON'dan ${jsonQuestions.size} soru yüklendi")
                
                // 2. Arka planda Firestore sync
                coroutineScope.launch {
                    syncFirestoreQuestions()
                }
                
                isInitialized = true
            } catch (e: Exception) {
                Log.e(TAG, "❌ Initialize hatası: ${e.message}", e)
            }
        }
    }
    
    /**
     * JSON dosyasından soruları yükler (assets/questions.json)
     */
    private fun loadQuestionsFromJson() {
        try {
            val inputStream = context.assets.open("questions.json")
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<List<Question>>() {}.type
            jsonQuestions = gson.fromJson(reader, type)
            reader.close()
        } catch (e: Exception) {
            Log.e(TAG, "❌ JSON yükleme hatası: ${e.message}", e)
            jsonQuestions = emptyList()
        }
    }
    
    // ============================================
    // FIRESTORE SYNC
    // ============================================
    
    /**
     * Firestore'dan soruları senkronize eder
     * Sadece level 20+ için çalışır
     */
    private suspend fun syncFirestoreQuestions() {
        try {
            // Son sync zamanını kontrol et
            val lastSync = cachedQuestionDao.getLastSyncTime(FIRESTORE_SYNC_LEVEL)
            val now = System.currentTimeMillis()
            
            if (lastSync != null && (now - lastSync) < SYNC_INTERVAL_MS) {
                Log.d(TAG, "⏭️ Firestore sync atlandı (son sync: ${(now - lastSync) / 1000 / 60} dakika önce)")
                return
            }
            
            Log.d(TAG, "⏳ Firestore sync başlatılıyor (level $FIRESTORE_SYNC_LEVEL+)...")
            
            // Firestore'dan level 20+ soruları çek
            val snapshot = firestore.collection("questions")
                .whereGreaterThanOrEqualTo("level", FIRESTORE_SYNC_LEVEL)
                .get()
                .await()
            
            val firestoreQuestions = snapshot.documents.mapNotNull { doc ->
                try {
                    doc.toObject(FirestoreQuestion::class.java)?.let { fq ->
                        CachedQuestion(
                            id = fq.id,
                            questionText = fq.questionText,
                            optionA = fq.optionA,
                            optionB = fq.optionB,
                            optionC = fq.optionC,
                            optionD = fq.optionD,
                            correctAnswer = fq.correctAnswer,
                            imageName = fq.imageName,
                            level = fq.level,
                            category = fq.category,
                            difficulty = fq.difficulty,
                            explanation = fq.explanation ?: "",
                            tags = gson.toJson(fq.tags ?: emptyList()),
                            points = fq.points,
                            timeLimit = fq.timeLimit,
                            isVerified = fq.isVerified,
                            authorId = fq.authorId,
                            createdAt = fq.createdAt,
                            downloadedAt = now,
                            isFromFirestore = true
                        )
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Soru parse hatası: ${e.message}")
                    null
                }
            }
            
            if (firestoreQuestions.isNotEmpty()) {
                // Room'a kaydet
                cachedQuestionDao.insertQuestions(firestoreQuestions)
                Log.d(TAG, "✅ Firestore'dan ${firestoreQuestions.size} soru indirildi ve kaydedildi")
            } else {
                Log.d(TAG, "ℹ️ Firestore'da yeni soru bulunamadı")
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "❌ Firestore sync hatası: ${e.message}", e)
        }
    }
    
    /**
     * Manuel sync tetikler (kullanıcı isteğiyle)
     */
    suspend fun forceSyncFirestore() {
        syncFirestoreQuestions()
    }
    
    // ============================================
    // QUESTION RETRIEVAL
    // ============================================
    
    /**
     * Belirli bir level için soruları getirir
     * Öncelik: Room (Firestore cache) > JSON
     */
    suspend fun getQuestionsForLevel(level: Int): List<Question> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. Önce Room'dan kontrol et
                val cachedQuestions = cachedQuestionDao.getQuestionsByLevel(level)
                
                if (cachedQuestions.isNotEmpty()) {
                    Log.d(TAG, "📦 Level $level için ${cachedQuestions.size} soru Room'dan alındı")
                    return@withContext cachedQuestions.map { it.toQuestion() }
                }
                
                // 2. Room'da yoksa JSON'dan al
                val jsonQuestionsForLevel = jsonQuestions.filter { it.level == level }
                
                if (jsonQuestionsForLevel.isNotEmpty()) {
                    Log.d(TAG, "📄 Level $level için ${jsonQuestionsForLevel.size} soru JSON'dan alındı")
                    return@withContext jsonQuestionsForLevel
                }
                
                // 3. Hiçbir yerde yoksa boş liste
                Log.w(TAG, "⚠️ Level $level için soru bulunamadı!")
                emptyList()
                
            } catch (e: Exception) {
                Log.e(TAG, "❌ Soru getirme hatası: ${e.message}", e)
                emptyList()
            }
        }
    }
    
    /**
     * Belirli bir level için soruları Flow olarak döner (reactive)
     */
    fun getQuestionsForLevelFlow(level: Int): Flow<List<Question>> {
        return cachedQuestionDao.getQuestionsByLevelFlow(level)
            .map { cachedQuestions ->
                if (cachedQuestions.isNotEmpty()) {
                    cachedQuestions.map { it.toQuestion() }
                } else {
                    jsonQuestions.filter { it.level == level }
                }
            }
    }
    
    /**
     * Tüm soruları getirir (önce Room, sonra JSON)
     */
    suspend fun getAllQuestions(): List<Question> {
        return withContext(Dispatchers.IO) {
            val cachedQuestions = cachedQuestionDao.getAllQuestions()
            val cachedIds = cachedQuestions.map { it.id }.toSet()
            
            // Room'daki soruları al
            val allQuestions = cachedQuestions.map { it.toQuestion() }.toMutableList()
            
            // JSON'dan Room'da olmayanları ekle
            jsonQuestions.forEach { jsonQ ->
                if (jsonQ.id !in cachedIds) {
                    allQuestions.add(jsonQ)
                }
            }
            
            allQuestions.sortedBy { it.id }
        }
    }
    
    /**
     * Belirli bir kategorideki soruları getirir
     */
    suspend fun getQuestionsByCategory(category: String): List<Question> {
        return withContext(Dispatchers.IO) {
            val cachedQuestions = cachedQuestionDao.getQuestionsByCategory(category)
            
            if (cachedQuestions.isNotEmpty()) {
                cachedQuestions.map { it.toQuestion() }
            } else {
                jsonQuestions.filter { it.category == category }
            }
        }
    }
    
    // ============================================
    // STATISTICS
    // ============================================
    
    /**
     * İstatistik bilgilerini döner
     */
    suspend fun getStatistics(): QuestionStatistics {
        return withContext(Dispatchers.IO) {
            val cachedCount = cachedQuestionDao.getQuestionCount()
            val firestoreCount = cachedQuestionDao.getFirestoreQuestionCount()
            val jsonCount = jsonQuestions.size
            
            QuestionStatistics(
                totalQuestions = cachedCount + jsonCount,
                cachedQuestions = cachedCount,
                firestoreQuestions = firestoreCount,
                jsonQuestions = jsonCount,
                lastSyncTime = cachedQuestionDao.getLastSyncTime(FIRESTORE_SYNC_LEVEL)
            )
        }
    }
    
    /**
     * Belirli bir level için soru var mı kontrol eder
     */
    suspend fun hasQuestionsForLevel(level: Int): Boolean {
        return withContext(Dispatchers.IO) {
            // Önce Room'da kontrol et
            if (cachedQuestionDao.hasQuestionsForLevel(level)) {
                return@withContext true
            }
            
            // Sonra JSON'da kontrol et
            jsonQuestions.any { it.level == level }
        }
    }
    
    // ============================================
    // CACHE MANAGEMENT
    // ============================================
    
    /**
     * Cache'i temizler
     */
    suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            cachedQuestionDao.deleteAllQuestions()
            Log.d(TAG, "🗑️ Cache temizlendi")
        }
    }
    
    /**
     * Sadece Firestore sorularını temizler
     */
    suspend fun clearFirestoreCache() {
        withContext(Dispatchers.IO) {
            cachedQuestionDao.deleteFirestoreQuestions()
            Log.d(TAG, "🗑️ Firestore cache temizlendi")
        }
    }
}

// ============================================
// DATA CLASSES
// ============================================

/**
 * Firestore'dan gelen soru formatı
 */
data class FirestoreQuestion(
    val id: Int = 0,
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String = "",
    val correctAnswer: String = "",
    val imageName: String = "",
    val level: Int = 1,
    val category: String = "GENERAL",
    val difficulty: String = "MEDIUM",
    val explanation: String? = null,
    val tags: List<String>? = null,
    val points: Int = 10,
    val timeLimit: Int = 30,
    val isVerified: Boolean = true,
    val authorId: String = "system",
    val createdAt: Long = 0
)

/**
 * İstatistik bilgileri
 */
data class QuestionStatistics(
    val totalQuestions: Int,
    val cachedQuestions: Int,
    val firestoreQuestions: Int,
    val jsonQuestions: Int,
    val lastSyncTime: Long?
)

/**
 * CachedQuestion'ı Question'a dönüştürür
 */
fun CachedQuestion.toQuestion(): Question {
    return Question(
        id = id,
        questionText = questionText,
        optionA = optionA,
        optionB = optionB,
        optionC = optionC,
        optionD = optionD,
        correctAnswer = correctAnswer,
        imageName = imageName,
        level = level,
        category = category
    )
}
