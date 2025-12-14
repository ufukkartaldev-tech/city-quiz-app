package com.example.oyun.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * Room Database Entity - Firestore'dan indirilen sorular için
 */
@Entity(tableName = "cached_questions")
data class CachedQuestion(
    @PrimaryKey
    val id: Int,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String,
    val correctAnswer: String,
    val imageName: String,
    val level: Int,
    val category: String,
    val difficulty: String,
    val explanation: String,
    val tags: String, // JSON string olarak saklanacak
    val points: Int,
    val timeLimit: Int,
    val isVerified: Boolean,
    val authorId: String,
    val createdAt: Long,
    val downloadedAt: Long = System.currentTimeMillis(),
    val isFromFirestore: Boolean = true
)

/**
 * Question DAO - Veritabanı işlemleri
 */
@Dao
interface CachedQuestionDao {
    
    // ============================================
    // INSERT İŞLEMLERİ
    // ============================================
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestion(question: CachedQuestion)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<CachedQuestion>)
    
    // ============================================
    // QUERY İŞLEMLERİ
    // ============================================
    
    @Query("SELECT * FROM cached_questions WHERE level = :level ORDER BY id ASC")
    suspend fun getQuestionsByLevel(level: Int): List<CachedQuestion>
    
    @Query("SELECT * FROM cached_questions WHERE level = :level ORDER BY id ASC")
    fun getQuestionsByLevelFlow(level: Int): Flow<List<CachedQuestion>>
    
    @Query("SELECT * FROM cached_questions WHERE category = :category ORDER BY id ASC")
    suspend fun getQuestionsByCategory(category: String): List<CachedQuestion>
    
    @Query("SELECT * FROM cached_questions WHERE difficulty = :difficulty ORDER BY id ASC")
    suspend fun getQuestionsByDifficulty(difficulty: String): List<CachedQuestion>
    
    @Query("SELECT * FROM cached_questions WHERE level = :level AND category = :category")
    suspend fun getQuestionsByLevelAndCategory(level: Int, category: String): List<CachedQuestion>
    
    @Query("SELECT * FROM cached_questions WHERE isFromFirestore = 1")
    suspend fun getFirestoreQuestions(): List<CachedQuestion>
    
    @Query("SELECT * FROM cached_questions ORDER BY id ASC")
    suspend fun getAllQuestions(): List<CachedQuestion>
    
    // ============================================
    // COUNT İŞLEMLERİ
    // ============================================
    
    @Query("SELECT COUNT(*) FROM cached_questions")
    suspend fun getQuestionCount(): Int
    
    @Query("SELECT COUNT(*) FROM cached_questions WHERE level = :level")
    suspend fun getQuestionCountByLevel(level: Int): Int
    
    @Query("SELECT COUNT(*) FROM cached_questions WHERE isFromFirestore = 1")
    suspend fun getFirestoreQuestionCount(): Int
    
    // ============================================
    // DELETE İŞLEMLERİ
    // ============================================
    
    @Query("DELETE FROM cached_questions WHERE level = :level")
    suspend fun deleteQuestionsByLevel(level: Int)
    
    @Query("DELETE FROM cached_questions WHERE isFromFirestore = 1")
    suspend fun deleteFirestoreQuestions()
    
    @Query("DELETE FROM cached_questions")
    suspend fun deleteAllQuestions()
    
    // ============================================
    // UPDATE İŞLEMLERİ
    // ============================================
    
    @Query("UPDATE cached_questions SET downloadedAt = :timestamp WHERE level >= :startLevel")
    suspend fun updateDownloadTimestamp(startLevel: Int, timestamp: Long)
    
    // ============================================
    // SYNC KONTROL
    // ============================================
    
    @Query("SELECT MAX(downloadedAt) FROM cached_questions WHERE level >= :level")
    suspend fun getLastSyncTime(level: Int): Long?
    
    @Query("SELECT EXISTS(SELECT 1 FROM cached_questions WHERE level = :level LIMIT 1)")
    suspend fun hasQuestionsForLevel(level: Int): Boolean
}
