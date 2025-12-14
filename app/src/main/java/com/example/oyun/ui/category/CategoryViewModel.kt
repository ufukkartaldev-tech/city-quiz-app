package com.example.oyun.ui.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.oyun.R
import com.example.oyun.data.HybridQuestionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Kategori Modeli
 */
data class QuestionCategory(
    val code: String,
    val name: String,
    val description: String,
    val icon: String, // Emoji veya drawable resource
    val color: Int, // Renk kodu
    val questionCount: Int = 0,
    val isLocked: Boolean = false,
    val requiredLevel: Int = 1
)

@HiltViewModel
class CategoryViewModel @Inject constructor(
    private val hybridRepository: HybridQuestionRepository
) : ViewModel() {

    private val _categories = MutableStateFlow<List<QuestionCategory>>(emptyList())
    val categories: StateFlow<List<QuestionCategory>> = _categories.asStateFlow()

    private val _categoryStats = MutableStateFlow<Map<String, Int>>(emptyMap())
    val categoryStats: StateFlow<Map<String, Int>> = _categoryStats.asStateFlow()

    /**
     * Kategorileri yükler
     */
    fun loadCategories() {
        viewModelScope.launch {
            try {
                // Tüm soruları al
                val allQuestions = hybridRepository.getAllQuestions()
                
                // Her kategorideki soru sayısını hesapla
                val stats = allQuestions.groupBy { it.category }
                    .mapValues { it.value.size }
                
                _categoryStats.value = stats
                
                // Kategori listesini oluştur
                val categoryList = listOf(
                    QuestionCategory(
                        code = "GEOGRAPHY",
                        name = "Coğrafya",
                        description = "Şehirler, göller, nehirler",
                        icon = "🌍",
                        color = 0xFF4CAF50.toInt(),
                        questionCount = stats["GEOGRAPHY"] ?: 0
                    ),
                    QuestionCategory(
                        code = "HISTORY",
                        name = "Tarih",
                        description = "Osmanlı, Cumhuriyet dönemi",
                        icon = "📜",
                        color = 0xFF9C27B0.toInt(),
                        questionCount = stats["HISTORY"] ?: 0
                    ),
                    QuestionCategory(
                        code = "CULTURE",
                        name = "Kültür",
                        description = "Gelenekler, UNESCO mirası",
                        icon = "🎭",
                        color = 0xFFFF9800.toInt(),
                        questionCount = stats["CULTURE"] ?: 0
                    ),
                    QuestionCategory(
                        code = "SPORTS",
                        name = "Spor",
                        description = "Futbol, olimpiyatlar",
                        icon = "⚽",
                        color = 0xFF2196F3.toInt(),
                        questionCount = stats["SPORTS"] ?: 0
                    ),
                    QuestionCategory(
                        code = "GENERAL",
                        name = "Genel Kültür",
                        description = "Çeşitli konular",
                        icon = "📚",
                        color = 0xFF607D8B.toInt(),
                        questionCount = stats["GENERAL"] ?: 0
                    ),
                    QuestionCategory(
                        code = "SCIENCE",
                        name = "Bilim",
                        description = "Bilimsel konular",
                        icon = "🔬",
                        color = 0xFF00BCD4.toInt(),
                        questionCount = stats["SCIENCE"] ?: 0,
                        isLocked = stats["SCIENCE"] == 0,
                        requiredLevel = 10
                    ),
                    QuestionCategory(
                        code = "ART",
                        name = "Sanat",
                        description = "Müzik, edebiyat, sinema",
                        icon = "🎨",
                        color = 0xFFE91E63.toInt(),
                        questionCount = stats["ART"] ?: 0,
                        isLocked = stats["ART"] == 0,
                        requiredLevel = 15
                    )
                )
                
                _categories.value = categoryList
                
            } catch (e: Exception) {
                android.util.Log.e("CategoryViewModel", "Kategori yükleme hatası: ${e.message}", e)
                _categories.value = emptyList()
            }
        }
    }

    /**
     * Belirli bir kategorideki soruları getirir
     */
    suspend fun getQuestionsForCategory(categoryCode: String): Int {
        return try {
            val questions = hybridRepository.getQuestionsByCategory(categoryCode)
            questions.size
        } catch (e: Exception) {
            0
        }
    }
}
