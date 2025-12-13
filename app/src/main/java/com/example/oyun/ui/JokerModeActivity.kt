package com.example.oyun.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.oyun.databinding.ActivityJokerModeBinding
import com.example.oyun.data.QuestionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JokerModeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJokerModeBinding

    @Inject
    lateinit var questionManager: QuestionManager

    private var currentQuestion: com.example.oyun.data.JokerQuestion? = null
    private var correctCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJokerModeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadQuestion()
        setupUI()
    }

    private fun setupUI() {
        binding.submitButton.setOnClickListener {
            checkAnswer()
        }
    }

    private fun loadQuestion() {
        currentQuestion = questionManager.getRandomJokerQuestion()
        currentQuestion?.let {
            binding.questionText.text = it.questionText
            binding.answerInput.text?.clear()
        } ?: run {
            Toast.makeText(this, "Soru bulunamadı!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun checkAnswer() {
        val userAnswer = binding.answerInput.text.toString().trim()
        val isCorrect = currentQuestion?.correctAnswers?.any { 
            it.equals(userAnswer, ignoreCase = true) 
        } ?: false

        if (isCorrect) {
            correctCount++
            Toast.makeText(this, "✅ Doğru! ($correctCount/3)", Toast.LENGTH_SHORT).show()
            
            if (correctCount >= 3) {
                // Joker kazanıldı
                val intent = Intent(this, JokerRewardActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                loadQuestion()
            }
        } else {
            Toast.makeText(this, "❌ Yanlış! Tekrar dene.", Toast.LENGTH_SHORT).show()
            binding.answerInput.text?.clear()
        }
    }
}
