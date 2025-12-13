package com.example.oyun.ui.game

import android.content.Context
import android.widget.Toast
import com.example.oyun.R
import com.example.oyun.data.Question
import com.example.oyun.data.QuestionManager
import com.example.oyun.databinding.ActivityGameBinding

import com.example.oyun.managers.TimeManager
import com.example.oyun.managers.UIManager

/**
 * Joker işlemlerini yöneten sınıf
 */
class JokerHandler(
    private val context: Context,
    private val binding: ActivityGameBinding,
    private val gameViewModel: GameViewModel,
    private val questionManager: QuestionManager,
    private val uiManager: UIManager,
    private val timerManager: TimeManager
) {
    // Çift tıklama koruması için flag'ler (her joker için ayrı)
    private var isProcessingFiftyFifty = false
    private var isProcessingSkip = false
    private var isProcessingGainLife = false

    /**
     * 50-50 jokerini uygular
     */
    fun applyFiftyFifty(currentQuestion: Question?) {
        // Çift tıklama koruması
        if (isProcessingFiftyFifty) {
            Toast.makeText(context, "Lütfen bekleyin...", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Joker sayısı kontrolü
        val jokerCount = gameViewModel.uiState.value.jokerInventory.fiftyFifty
        if (jokerCount <= 0) {
            Toast.makeText(context, "50-50 jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
            return
        }
        
        isProcessingFiftyFifty = true
        
        currentQuestion?.let { question ->
            val correctIndex = question.correctAnswerIndex
            val wrongIndices = (0..3).toMutableList().apply { remove(correctIndex) }
            wrongIndices.shuffle()
            val indicesToHide = wrongIndices.take(2)
            
            uiManager.hideTwoWrongAnswers(indicesToHide)
            gameViewModel.useFiftyFiftyJoker()
        }
        
        // 500ms sonra flag'i sıfırla
        binding.root.postDelayed({
            isProcessingFiftyFifty = false
        }, 500)
    }

    /**
     * Soru atlama jokerini kullanır
     */
    fun useSkipJoker(onComplete: () -> Unit) {
        // Çift tıklama koruması
        if (isProcessingSkip) {
            Toast.makeText(context, "Lütfen bekleyin...", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Joker sayısı kontrolü
        val jokerCount = gameViewModel.uiState.value.jokerInventory.skip
        if (jokerCount <= 0) {
            Toast.makeText(context, "Atlama jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
            return
        }
        
        isProcessingSkip = true
        
        timerManager.stopTimer()
        gameViewModel.useSkipJoker()
        
        // Kısa gecikme sonrası yeni soru yükle
        binding.root.postDelayed({
            gameViewModel.loadNextQuestion()
            isProcessingSkip = false
            onComplete()
        }, 500)
    }

    /**
     * Can kazanma jokerini kullanır
     */
    fun useGainLifeJoker(onAnswerProcessed: (Boolean) -> Unit) {
        // Çift tıklama koruması
        if (isProcessingGainLife) {
            Toast.makeText(context, "Lütfen bekleyin...", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Joker sayısı kontrolü
        val jokerCount = gameViewModel.uiState.value.jokerInventory.gainLife
        if (jokerCount <= 0) {
            Toast.makeText(context, "Can kazanma jokeriniz kalmadı!", Toast.LENGTH_SHORT).show()
            return
        }
        
        isProcessingGainLife = true
        
        timerManager.pauseTimer()
        
        val jokerQuestion = questionManager.getRandomJokerQuestion()
        
        if (jokerQuestion != null) {
            showJokerQuestionUI(jokerQuestion, onAnswerProcessed)
        } else {
            handleJokerQuestionNotFound(onAnswerProcessed)
        }
    }

    private fun showJokerQuestionUI(
        jokerQuestion: com.example.oyun.data.JokerQuestion,
        onAnswerProcessed: (Boolean) -> Unit
    ) {
        uiManager.showJokerQuestionLayout()
        binding.jokerQuestionText.text = jokerQuestion.questionText
        binding.jokerAnswerInput.text?.clear()
        
        binding.jokerSubmitButton.setOnClickListener {
            val userAnswer = binding.jokerAnswerInput.text.toString().trim()
            
            // Input validation
            if (userAnswer.isEmpty()) {
                Toast.makeText(
                    context,
                    "Lütfen bir cevap girin",
                    Toast.LENGTH_SHORT
                ).show()
                binding.jokerAnswerInput.error = "Cevap boş olamaz"
                return@setOnClickListener
            }
            
            val isCorrect = checkJokerAnswer(userAnswer, jokerQuestion.correctAnswers)
            
            if (isCorrect) {
                Toast.makeText(
                    context,
                    context.getString(R.string.correct_answer_gain_life),
                    Toast.LENGTH_SHORT
                ).show()
                gameViewModel.addLife()
            } else {
                Toast.makeText(
                    context,
                    context.getString(R.string.wrong_answer),
                    Toast.LENGTH_SHORT
                ).show()
            }
            
            uiManager.showGameLayout()
            isProcessingGainLife = false  // Flag'i sıfırla
            timerManager.resumeTimer { timeIsUp ->
                if (timeIsUp) onAnswerProcessed(false)
            }
        }
    }

    private fun handleJokerQuestionNotFound(onAnswerProcessed: (Boolean) -> Unit) {
        Toast.makeText(
            context,
            context.getString(R.string.joker_not_found_refund),
            Toast.LENGTH_SHORT
        ).show()
        gameViewModel.returnGainLifeJoker()
        uiManager.showGameLayout()
        isProcessingGainLife = false  // Flag'i sıfırla
        timerManager.resumeTimer { timeIsUp ->
            if (timeIsUp) onAnswerProcessed(false)
        }
    }

    private fun checkJokerAnswer(userAnswer: String, correctAnswers: List<String>): Boolean {
        val normalizedUserAnswer = normalizeAnswer(userAnswer)
        return correctAnswers.any { correctAnswer ->
            val normalizedCorrect = normalizeAnswer(correctAnswer)
            normalizedUserAnswer == normalizedCorrect
        }
    }

    /**
     * Türkçe karakter ve boşluk farklarını yok sayarak cevabı normalize eder
     */
    private fun normalizeAnswer(answer: String): String {
        return answer
            .lowercase()
            .replace(Regex("[çÇ]"), "c")
            .replace(Regex("[ğĞ]"), "g")
            .replace(Regex("[ıİ]"), "i")
            .replace(Regex("[öÖ]"), "o")
            .replace(Regex("[şŞ]"), "s")
            .replace(Regex("[üÜ]"), "u")
            .replace(Regex("\\s+"), "") // Boşlukları kaldır
            .replace(Regex("[^a-z0-9]"), "") // Sadece harf ve rakamları tut
    }
}
