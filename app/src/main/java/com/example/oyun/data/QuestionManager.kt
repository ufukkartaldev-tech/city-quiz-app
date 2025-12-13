package com.example.oyun.data

import android.content.Context
import android.util.Log // ⬅️ Hata ayıklama için Log'u ekledik
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuestionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson // ⬅️ Gson'u Hilt ile Enjekte Etmek daha iyi
) {
    // Soru verilerini tutan haritalar ve listeler
    private lateinit var questionsByLevel: Map<Int, List<Question>>
    private lateinit var allJokerQuestions: List<JokerQuestion>

    init {
        // Init bloğunda yüklemeyi başlat
        loadAllQuestions()
        loadJokerQuestions()
    }

    /**
     * assets/questions.json dosyasından tüm seviye sorularını yükler.
     */
    private fun loadAllQuestions() {
        try {
            // 'use' bloğu, işlem bittiğinde kaynakların (InputStreamReader) otomatik kapanmasını sağlar.
            context.assets.open("questions.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->

                    val listType = object : TypeToken<List<LevelQuestions>>() {}.type
                    val loadedLevels: List<LevelQuestions> = gson.fromJson(reader, listType)

                    // Soruları seviye anahtarıyla eşleştirip Map'e atıyoruz.
                    questionsByLevel = loadedLevels.associate { levelData ->
                        levelData.level to levelData.questions.map { it.toQuestion(context) }
                    }
                    Log.d("QuestionManager", "Toplam ${questionsByLevel.size} seviye sorusu yüklendi.")
                }
            }
        } catch (e: Exception) {
            // Hata olursa hem Log'a yazdır hem de boş Map at, uygulamanın çökmesini önle.
            Log.e("QuestionManager", "Sorular yüklenirken hata oluştu: ${e.message}")
            e.printStackTrace()
            questionsByLevel = emptyMap()
        }
    }

    /**
     * Joker sorularını yükler (Eğer assets/joker_questions.json varsa buradan okuyabilirsin).
     */
    private fun loadJokerQuestions() {
        try {
            context.assets.open("joker_questions.json").use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val listType = object : TypeToken<List<CityJokerData>>() {}.type
                    val loadedJokerData: List<CityJokerData> = gson.fromJson(reader, listType)

                    // Tüm şehirlerin joker sorularını tek bir listede toplarız
                    allJokerQuestions = loadedJokerData.flatMap { it.joker_questions }
                    Log.d("QuestionManager", "Joker soruları yüklendi: ${allJokerQuestions.size} adet.")
                }
            }
        } catch (e: Exception) {
            Log.e("QuestionManager", "Joker soruları yüklenirken hata oluştu: ${e.message}")
            e.printStackTrace()
            allJokerQuestions = emptyList() // Hata durumunda boş liste ata
        }
    }

    // --- DIŞARIYA AÇIK FONKSİYONLAR ---

    fun getQuestionsForLevel(level: Int): List<Question> {
        return questionsByLevel[level] ?: emptyList()
    }

    /**
     * Belirtilen seviye için soru olup olmadığını kontrol eder.
     */
    fun hasQuestionsForLevel(level: Int): Boolean {
        // 'containsKey' ile kontrol etmek daha hızlıdır, ancak değerin boş olup olmadığını
        // kontrol eden orijinal kodun mantığını korumak için bu şekilde bıraktık.
        return questionsByLevel[level]?.isNotEmpty() == true
    }

    fun getRandomJokerQuestion(): JokerQuestion? {
        return allJokerQuestions.randomOrNull()
    }
}

// JSON verisini Uygulama Soru formatına çeviren yardımcı fonksiyon
private fun QuestionData.toQuestion(context: Context): Question {
    val resourceId = if (this.imageName.isNotEmpty()) {
        // Resim isminden ID bulmaya çalışır (drawable klasöründe)
        // Eğer resim yoksa 0 (varsayılan) döner
        context.resources.getIdentifier(this.imageName.substringBefore("."), "drawable", context.packageName)
    } else 0
    return Question(resourceId, this.questionText, this.options, this.correctAnswerIndex, this.theme ?: "Genel")
}