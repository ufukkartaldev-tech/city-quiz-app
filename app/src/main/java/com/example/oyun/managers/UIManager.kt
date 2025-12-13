package com.example.oyun.managers

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.example.oyun.data.Question
import com.example.oyun.databinding.ActivityGameBinding
import com.example.oyun.R

class UIManager(
    private val activity: Activity,
    private val binding: ActivityGameBinding
) {
    var isAnswered = false

    // BUTONLARI LİSTEYE ALIYORUZ Kİ YÖNETMESİ KOLAY OLSUN
    // XML'deki ID'lerin (answerButton1, answerButton2...) doğru olduğundan emin ol
    private val answerButtons: List<Button> = listOf(
        binding.answerButton1,
        binding.answerButton2,
        binding.answerButton3,
        binding.answerButton4
    )

    fun showGameLayout() {
        // binding.gameLayout.visibility = View.VISIBLE
        // binding.jokerQuestionLayout.visibility = View.GONE
    }

    fun showJokerQuestionLayout() {
        // binding.jokerQuestionLayout.visibility = View.VISIBLE
        // binding.gameLayout.visibility = View.GONE
    }

    fun setQuestion(question: Question) {
        binding.questionImage.setImageDrawable(null)
        binding.questionText.text = question.questionText

        // Soru metni animasyonu
        binding.questionText.startAnimation(android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.fade_in_modern))

        if (question.imageResId != 0) {
            binding.questionImage.setImageResource(question.imageResId)
            binding.questionImage.visibility = View.VISIBLE
            // Soru resmi animasyonu
            binding.questionImage.startAnimation(android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.fade_in_modern))
        } else {
            binding.questionImage.visibility = View.GONE
        }
        resetButtonColors()
        isAnswered = false
    }

    fun displayAnswers(answers: List<String>) {
        resetButtonColors()
        answerButtons.forEachIndexed { index, btn ->
            if (index < answers.size) {
                btn.text = answers[index]
                btn.visibility = View.VISIBLE
                btn.isEnabled = true
                btn.alpha = 1.0f // Görünürlük ayarı
                
                // Cevap butonları animasyonu (hafif gecikmeli olabilir ama şimdilik doğrudan)
                btn.startAnimation(android.view.animation.AnimationUtils.loadAnimation(activity, R.anim.fade_in_modern))
            } else {
                btn.visibility = View.GONE
            }
        }
    }

    fun hideTwoWrongAnswers(indicesToHide: List<Int>) {
        indicesToHide.forEach { index ->
            val btn = answerButtons.getOrNull(index)
            btn?.visibility = View.INVISIBLE // Veya btn?.isEnabled = false yapılabilir
        }
    }

    // Tıklama Olayı: Hangi butona (index) tıklandığını söyler
    fun setAnswerButtonListeners(correctIndex: Int, onAnswerSelected: (Int) -> Unit) {
        answerButtons.forEachIndexed { index, btn ->
            btn.setOnClickListener {
                if (!isAnswered) {
                    isAnswered = true
                    handleQuizAnswerVisuals(index, correctIndex)
                    onAnswerSelected(index) // Tıklanan index'i gönderiyoruz
                }
            }
        }
    }

    private fun handleQuizAnswerVisuals(selectedIndex: Int, correctIndex: Int) {
        answerButtons.forEach { it.isEnabled = false }

        // Seçilen butonun rengi
        answerButtons.getOrNull(selectedIndex)?.backgroundTintList =
            ColorStateList.valueOf(if (selectedIndex == correctIndex) Color.GREEN else Color.RED)

        // Eğer yanlış seçtiyse doğruyu da yeşil yap
        if (selectedIndex != correctIndex) {
            answerButtons.getOrNull(correctIndex)?.backgroundTintList =
                ColorStateList.valueOf(Color.GREEN)
        }
    }

    private fun resetButtonColors() {
        val defaultColor = ColorStateList.valueOf(Color.LTGRAY) // Varsayılan gri
        answerButtons.forEach {
            it.backgroundTintList = defaultColor
            it.isEnabled = true
            it.visibility = View.VISIBLE
        }
    }

    fun updateTimer(timeLeft: Int) {
        binding.timerText.text = "⏱️ $timeLeft sn"
        val color = when {
            timeLeft > 10 -> Color.BLACK
            timeLeft > 5 -> Color.rgb(255, 165, 0) // Turuncu
            else -> Color.RED
        }
        binding.timerText.setTextColor(color)
    }

    fun updateJokerInfo(fifty: Int, skip: Int, gain: Int) {
        binding.jokerFiftyFifty.text = "50/50\n($fifty)"
        binding.jokerSkip.text = "Geç\n($skip)"
        binding.jokerGainLife.text = "Can\n($gain)"
        
        // Joker sayısına göre butonları enable/disable et
        binding.jokerFiftyFifty.isEnabled = fifty > 0
        binding.jokerSkip.isEnabled = skip > 0
        binding.jokerGainLife.isEnabled = gain > 0
        
        // Görsel geri bildirim için alpha ayarla
        binding.jokerFiftyFifty.alpha = if (fifty > 0) 1.0f else 0.5f
        binding.jokerSkip.alpha = if (skip > 0) 1.0f else 0.5f
        binding.jokerGainLife.alpha = if (gain > 0) 1.0f else 0.5f
    }

    fun updateLives(left: Int, max: Int) {
        binding.livesText.text = "❤️".repeat(left.coerceAtLeast(0)) + "🖤".repeat((max - left).coerceAtLeast(0))
    }

    fun updateLevel(level: Int) {
        binding.levelText.text = "Seviye: $level"
    }

    fun updateScore(score: Int) {
        binding.scoreText.text = "Skor: $score"
    }

    fun showTimeUpFeedback(correctAnswer: String) {
        binding.questionText.text = "Süre Bitti!\nDoğru cevap: $correctAnswer"
        isAnswered = true
        answerButtons.forEach { it.isEnabled = false }
    }

    fun showLevelUpMessage(level: Int) {
        Toast.makeText(activity, "Tebrikler! Seviye $level'e geçtiniz!", Toast.LENGTH_LONG).show()
    }
}