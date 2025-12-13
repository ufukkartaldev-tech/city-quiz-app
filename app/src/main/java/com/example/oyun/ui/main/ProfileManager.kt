package com.example.oyun.ui.main

import android.content.Context
import android.content.SharedPreferences
import com.example.oyun.data.QuestionManager
import com.example.oyun.data.remote.AuthRepository

/**
 * Profil ve seviye yönetimini sağlayan sınıf
 */
class ProfileManager(
    private val prefs: SharedPreferences,
    private val authRepository: AuthRepository,
    private val questionManager: QuestionManager
) {

    fun getActiveUserDisplayName(): String {
        return authRepository.getUserDisplayName()
    }

    fun getCurrentLevel(): Int {
        val activeUser = authRepository.getUserDisplayName()
        val userLevelKey = "profile_${activeUser}_current_level"  // current_level olarak değiştirildi
        var level = prefs.getInt(userLevelKey, 1)

        // Seviye geçerliliğini kontrol et
        if (level < 1 || !questionManager.hasQuestionsForLevel(level)) {
            level = 1
            prefs.edit().putInt(userLevelKey, 1).apply()
        }

        return level
    }

    fun getJokerCounts(): JokerCounts {
        val activeUser = authRepository.getActiveUser()
        return JokerCounts(
            fiftyFifty = prefs.getInt("profile_${activeUser}_joker_fiftyfifty_count", 0),
            skip = prefs.getInt("profile_${activeUser}_joker_skip_count", 0),
            gainLife = prefs.getInt("profile_${activeUser}_joker_gainlife_count", 0)
        )
    }

    fun addJokerRewards(fiftyFifty: Int, skip: Int, gainLife: Int) {
        val activeUser = authRepository.getActiveUser()
        val currentCounts = getJokerCounts()

        prefs.edit().apply {
            putInt("profile_${activeUser}_joker_fiftyfifty_count", currentCounts.fiftyFifty + fiftyFifty)
            putInt("profile_${activeUser}_joker_skip_count", currentCounts.skip + skip)
            putInt("profile_${activeUser}_joker_gainlife_count", currentCounts.gainLife + gainLife)
            apply()
        }
    }

    data class JokerCounts(
        val fiftyFifty: Int,
        val skip: Int,
        val gainLife: Int
    )
}
