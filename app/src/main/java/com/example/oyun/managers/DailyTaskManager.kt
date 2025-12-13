package com.example.oyun.managers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.example.oyun.data.DailyTask
import com.example.oyun.data.DailyTaskData
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DailyTaskManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val prefs: SharedPreferences
) {

    private var activeUser = "Misafir"

    init {
        activeUser = prefs.getString("last_active_user", "Misafir") ?: "Misafir"
        checkDailyReset()
    }

    companion object {
        private const val LAST_RESET_DATE_KEY = "last_daily_reset_date"
    }

    // Günlük görevler (her gün aynı görevler)
    private val baseDailyTasks: List<DailyTask> by lazy {
        DailyTaskData.dailyTasks
    }

    // Günlük reset kontrolü
    private fun checkDailyReset() {
        val today = System.currentTimeMillis() / (1000 * 60 * 60 * 24)
        val lastReset = prefs.getLong(LAST_RESET_DATE_KEY, 0)

        if (today != lastReset) {
            resetDailyTasks()
            prefs.edit().putLong(LAST_RESET_DATE_KEY, today).apply()
        }
    }

    // Günlük görevleri sıfırla - Null safe
    private fun resetDailyTasks() {
        try {
            val editor = prefs.edit()
            baseDailyTasks.forEach { task ->
                editor.putInt("daily_${activeUser}_${task.id}_progress", 0)
                editor.putBoolean("daily_${activeUser}_${task.id}_completed", false)
            }
            editor.apply()
            Log.d("DailyTaskManager", "Günlük görevler sıfırlandı")
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görevler sıfırlanırken hata: ${e.message}")
        }
    }

    // Mevcut günlük görevleri getir - Null safe
    fun getDailyTasks(): List<DailyTask> {
        return try {
            baseDailyTasks.map { baseTask ->
                val progress = prefs.getInt("daily_${activeUser}_${baseTask.id}_progress", 0)
                val isCompleted = prefs.getBoolean("daily_${activeUser}_${baseTask.id}_completed", false)

                baseTask.copy(
                    progress = progress,
                    isCompleted = isCompleted
                )
            }
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görevler alınırken hata: ${e.message}")
            emptyList()
        }
    }

    // Görev ilerlemesi güncelle
    private fun updateTaskProgress(taskId: Int, increment: Int = 1) {
        try {
            val task = baseDailyTasks.find { it.id == taskId } ?: return
            val currentProgress = prefs.getInt("daily_${activeUser}_${taskId}_progress", 0)
            val isCompleted = prefs.getBoolean("daily_${activeUser}_${taskId}_completed", false)

            if (!isCompleted) {
                val newProgress = (currentProgress + increment).coerceAtMost(task.maxProgress)
                prefs.edit().putInt("daily_${activeUser}_${taskId}_progress", newProgress).apply()

                Log.d("DailyTaskManager", "Görev $taskId: $currentProgress -> $newProgress")

                // Görev tamamlandı mı kontrol et
                if (newProgress >= task.maxProgress) {
                    completeTask(taskId, task.reward)
                }
            }
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görev güncellenirken hata: ${e.message}")
        }
    }

    // Görev tamamlandığında ödül ver
    private fun completeTask(taskId: Int, reward: String) {
        try {
            prefs.edit().putBoolean("daily_${activeUser}_${taskId}_completed", true).apply()

            // Joker ödülü ver (reward string'inden sayıyı çıkar)
            val rewardAmount = reward.filter { it.isDigit() }.toIntOrNull() ?: 1

            val currentFiftyFifty = prefs.getInt("profile_${activeUser}_joker_fiftyfifty_count", 0)
            val currentSkip = prefs.getInt("profile_${activeUser}_joker_skip_count", 0)
            val currentGainLife = prefs.getInt("profile_${activeUser}_joker_gainlife_count", 0)

            prefs.edit().apply {
                putInt("profile_${activeUser}_joker_fiftyfifty_count", currentFiftyFifty + rewardAmount)
                putInt("profile_${activeUser}_joker_skip_count", currentSkip + rewardAmount)
                putInt("profile_${activeUser}_joker_gainlife_count", currentGainLife + rewardAmount)
                apply()
            }

            // Görev adını bul ve bildirim göster
            val task = baseDailyTasks.find { it.id == taskId }
            task?.let {
                Toast.makeText(context, "✅ Günlük görev tamamlandı: ${it.title}\n🎁 ${reward} kazandın!", Toast.LENGTH_LONG).show()
            }

            Log.d("DailyTaskManager", "Görev $taskId tamamlandı, $rewardAmount joker verildi")
        } catch (e: Exception) {
            Log.e("DailyTaskManager", "Görev tamamlanırken hata: ${e.message}")
        }
    }

    // Game event'leri için public fonksiyonlar
    fun onGameCompleted(usedJokers: Boolean) {
        updateTaskProgress(1) // Oyuncu görevi
        if (!usedJokers) {
            updateTaskProgress(3) // Saf Yetenek görevi
        }
    }

    fun onCorrectAnswer() {
        updateTaskProgress(2) // Bilgin görevi
    }

    fun onStreak(streakCount: Int) {
        if (streakCount >= 3) {
            updateTaskProgress(4) // Seri Katili görevi
        }
    }
}