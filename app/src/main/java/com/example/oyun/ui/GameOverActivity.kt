package com.example.oyun.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oyun.data.HighScore
import com.example.oyun.data.HighScoreDao
import com.example.oyun.data.remote.AuthRepository
import com.example.oyun.data.remote.FirebaseSyncService
import com.example.oyun.databinding.ActivityGameOverBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameOverActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGameOverBinding

    @Inject
    lateinit var highScoreDao: HighScoreDao

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var firebaseSyncService: FirebaseSyncService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGameOverBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("score", 0)
        val correctAnswers = intent.getIntExtra("correct_answers", 0)
        val totalQuestions = intent.getIntExtra("total_questions", 0)
        val level = intent.getIntExtra("level", 1)
        val gameWon = intent.getBooleanExtra("game_won", false)
        
        // Joker bilgilerini al
        val jokerFiftyFifty = intent.getIntExtra("joker_fiftyfifty", 0)
        val jokerSkip = intent.getIntExtra("joker_skip", 0)
        val jokerGainLife = intent.getIntExtra("joker_gainlife", 0)

        val accuracy = if (totalQuestions > 0) (correctAnswers * 100) / totalQuestions else 0

        // binding.gameOverTitle.text = if (gameWon) "Tebrikler!" else "Oyun Bitti!" // XML'de ID yok
        binding.scoreText.text = "Skorunuz: $score"
        binding.correctAnswersText.text = "Doğruluk: %$accuracy ($correctAnswers/$totalQuestions)"

        saveHighScore(score, correctAnswers, totalQuestions, level)
        
        // Joker ve seviye bilgilerini kaydet
        saveGameProgress(level, jokerFiftyFifty, jokerSkip, jokerGainLife)

        binding.retryButton.setOnClickListener {
            val intent = Intent(this, GameActivity::class.java)
            intent.putExtra("LEVEL", 1)
            intent.putExtra("IS_NEW_GAME", true)  // Tekrar dene = yeni oyun
            startActivity(intent)
            finish()
        }

        binding.mainMenuButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }


    private fun saveHighScore(score: Int, correctAnswers: Int, totalQuestions: Int, level: Int) {
        val activeUser = authRepository.getUserDisplayName()
        lifecycleScope.launch {
            try {
                val highScore = HighScore(
                    userName = activeUser,
                    score = score,
                    correctAnswers = correctAnswers,
                    totalQuestions = totalQuestions,
                    level = level,
                    timestamp = System.currentTimeMillis()
                )
                highScoreDao.insert(highScore)

                // İnternet varsa pending skorları Firebase'e senkron etmeyi dene
                if (com.example.oyun.utils.NetworkUtils.isNetworkAvailable(this@GameOverActivity)) {
                    try {
                        firebaseSyncService.syncUnsyncedScores()
                    } catch (e: Exception) {
                        android.util.Log.e("GameOver", "Firebase sync failed", e)
                        // Sessizce başarısız ol, offline mode devam eder
                    }
                } else {
                    android.util.Log.d("GameOver", "No internet connection, sync will happen later")
                }
            } catch (e: Exception) {
                android.util.Log.e("GameOver", "Error saving high score", e)
                android.widget.Toast.makeText(
                    this@GameOverActivity,
                    "Skor kaydedilemedi",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
    
    
    /**
     * Oyun bittiğinde joker ve seviye bilgilerini kaydeder
     * Oyun sonu olduğu için skor, can ve ilerleme sıfırlanır
     */
    private fun saveGameProgress(level: Int, jokerFiftyFifty: Int, jokerSkip: Int, jokerGainLife: Int) {
        // Tutarlılık için authRepository kullan
        val activeUser = authRepository.getUserDisplayName()
        
        prefs.edit().apply {
            // Oyun BİTTİĞİ için bu değerleri sıfırla
            putInt("profile_${activeUser}_score", 0)  // Skor sıfırla
            putInt("profile_${activeUser}_lives", 3)  // Canı başlangıç değerine sıfırla
            putInt("profile_${activeUser}_questions_answered", 0)  // İlerlemeyi sıfırla
            
            // Seviye ilerlemesini kaydet (ulaştığı son seviye)
            putInt("profile_${activeUser}_current_level", level)
            
            // Joker sayılarını kaydet
            putInt("profile_${activeUser}_joker_fiftyfifty_count", jokerFiftyFifty)
            putInt("profile_${activeUser}_joker_skip_count", jokerSkip)
            putInt("profile_${activeUser}_joker_gainlife_count", jokerGainLife)
            
            apply()
        }
    }
}
