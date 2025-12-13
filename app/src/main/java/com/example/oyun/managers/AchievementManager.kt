package com.example.oyun.managers

import android.content.Context
import android.content.SharedPreferences
import android.widget.Toast
import com.example.oyun.data.AchievementData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPreferences
) {

    private var activeUser = "Misafir"

    init {
        activeUser = prefs.getString("last_active_user", "Misafir") ?: "Misafir"
    }

    // Başarım kontrol ve unlock fonksiyonu
    private fun checkAndUnlockAchievement(achievementId: String) {
        val key = "profile_${activeUser}_achievement_${achievementId}"
        if (!prefs.getBoolean(key, false)) {
            prefs.edit().putBoolean(key, true).apply()

            // Başarım adını bul ve toast göster
            val achievement = AchievementData.achievements.find { it.id == achievementId }
            achievement?.let {
                Toast.makeText(context, "🏆 Başarım Kazanıldı: ${it.title}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // Çeşitli game event'leri için kontrol fonksiyonları
    fun checkFirstGame() {
        val gamesPlayed = prefs.getInt("profile_${activeUser}_games_played", 0)
        if (gamesPlayed == 1) {
            checkAndUnlockAchievement("first_game")
        }
        if (gamesPlayed == 10) {
            checkAndUnlockAchievement("games_10")
        }
    }

    fun checkFirstLevel() {
        val highestLevel = prefs.getInt("profile_${activeUser}_level", 1)
        if (highestLevel >= 2) {
            checkAndUnlockAchievement("first_level")
        }
        if (highestLevel >= 5) {
            checkAndUnlockAchievement("reach_level_5")
        }
        if (highestLevel >= 8) {
            checkAndUnlockAchievement("reach_level_8")
        }
    }

    fun checkQuestionsMilestone() {
        val totalCorrect = prefs.getInt("profile_${activeUser}_total_correct", 0)
        if (totalCorrect >= 25) {
            checkAndUnlockAchievement("questions_25")
        }
        if (totalCorrect >= 100) {
            checkAndUnlockAchievement("questions_100")
        }
    }

    fun checkStreakAchievement(currentStreak: Int) {
        if (currentStreak >= 5) {
            checkAndUnlockAchievement("streak_5")
        }
    }

    fun checkLevelCompletedNoJokers(usedJokerInLevel: Boolean) {
        if (!usedJokerInLevel) {
            checkAndUnlockAchievement("level_no_joker")
        }
    }

    fun checkPerfectGame(usedJokerInEntireGame: Boolean, completedAllLevels: Boolean) {
        if (!usedJokerInEntireGame && completedAllLevels) {
            checkAndUnlockAchievement("perfect_game")
        }
    }

    // Oyun istatistiklerini güncelle
    fun updateGameStats(correctAnswers: Int, totalQuestions: Int) {
        prefs.edit().apply {
            putInt("profile_${activeUser}_games_played",
                prefs.getInt("profile_${activeUser}_games_played", 0) + 1)
            putInt("profile_${activeUser}_total_correct",
                prefs.getInt("profile_${activeUser}_total_correct", 0) + correctAnswers)
            putInt("profile_${activeUser}_total_questions",
                prefs.getInt("profile_${activeUser}_total_questions", 0) + totalQuestions)
            apply()
        }

        // İstatistik güncellemesi sonrası kontroller
        checkFirstGame()
        checkQuestionsMilestone()
    }
}