package com.example.oyun.managers

import android.content.Context
import android.content.SharedPreferences
import com.example.oyun.data.AchievementData
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * AchievementManager için unit testler
 * Başarım sisteminin doğru çalıştığını test eder
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class AchievementManagerTest {

    private lateinit var achievementManager: AchievementManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putBoolean(any(), any()) } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockPrefs.getString("last_active_user", "Misafir") } returns "TestUser"
        every { mockPrefs.getBoolean(any(), false) } returns false
        every { mockPrefs.getInt(any(), 0) } returns 0

        achievementManager = AchievementManager(mockContext, mockPrefs)
    }

    @Test
    fun `checkFirstGame should unlock achievement on first game`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_games_played", 0) } returns 1

        // When
        achievementManager.checkFirstGame()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_first_game", true) }
    }

    @Test
    fun `checkFirstGame should unlock games_10 achievement after 10 games`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_games_played", 0) } returns 10

        // When
        achievementManager.checkFirstGame()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_games_10", true) }
    }

    @Test
    fun `checkFirstLevel should unlock achievement when reaching level 2`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_level", 1) } returns 2

        // When
        achievementManager.checkFirstLevel()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_first_level", true) }
    }

    @Test
    fun `checkFirstLevel should unlock reach_level_5 achievement`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_level", 1) } returns 5

        // When
        achievementManager.checkFirstLevel()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_reach_level_5", true) }
    }

    @Test
    fun `checkFirstLevel should unlock reach_level_8 achievement`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_level", 1) } returns 8

        // When
        achievementManager.checkFirstLevel()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_reach_level_8", true) }
    }

    @Test
    fun `checkQuestionsMilestone should unlock questions_25 achievement`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_total_correct", 0) } returns 25

        // When
        achievementManager.checkQuestionsMilestone()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_questions_25", true) }
    }

    @Test
    fun `checkQuestionsMilestone should unlock questions_100 achievement`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_total_correct", 0) } returns 100

        // When
        achievementManager.checkQuestionsMilestone()

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_questions_100", true) }
    }

    @Test
    fun `checkStreakAchievement should unlock on 5 streak`() {
        // When
        achievementManager.checkStreakAchievement(5)

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_streak_5", true) }
    }

    @Test
    fun `checkStreakAchievement should not unlock below 5 streak`() {
        // When
        achievementManager.checkStreakAchievement(4)

        // Then
        verify(exactly = 0) { mockEditor.putBoolean("profile_TestUser_achievement_streak_5", true) }
    }

    @Test
    fun `checkLevelCompletedNoJokers should unlock when no jokers used`() {
        // When
        achievementManager.checkLevelCompletedNoJokers(usedJokerInLevel = false)

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_level_no_joker", true) }
    }

    @Test
    fun `checkLevelCompletedNoJokers should not unlock when jokers used`() {
        // When
        achievementManager.checkLevelCompletedNoJokers(usedJokerInLevel = true)

        // Then
        verify(exactly = 0) { mockEditor.putBoolean("profile_TestUser_achievement_level_no_joker", true) }
    }

    @Test
    fun `checkPerfectGame should unlock when no jokers used and all levels completed`() {
        // When
        achievementManager.checkPerfectGame(
            usedJokerInEntireGame = false,
            completedAllLevels = true
        )

        // Then
        verify { mockEditor.putBoolean("profile_TestUser_achievement_perfect_game", true) }
    }

    @Test
    fun `checkPerfectGame should not unlock when jokers used`() {
        // When
        achievementManager.checkPerfectGame(
            usedJokerInEntireGame = true,
            completedAllLevels = true
        )

        // Then
        verify(exactly = 0) { mockEditor.putBoolean("profile_TestUser_achievement_perfect_game", true) }
    }

    @Test
    fun `updateGameStats should increment games played`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_games_played", 0) } returns 5

        // When
        achievementManager.updateGameStats(correctAnswers = 8, totalQuestions = 10)

        // Then
        verify { mockEditor.putInt("profile_TestUser_games_played", 6) }
    }

    @Test
    fun `updateGameStats should increment total correct answers`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_total_correct", 0) } returns 20

        // When
        achievementManager.updateGameStats(correctAnswers = 8, totalQuestions = 10)

        // Then
        verify { mockEditor.putInt("profile_TestUser_total_correct", 28) }
    }

    @Test
    fun `updateGameStats should increment total questions`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_total_questions", 0) } returns 50

        // When
        achievementManager.updateGameStats(correctAnswers = 8, totalQuestions = 10)

        // Then
        verify { mockEditor.putInt("profile_TestUser_total_questions", 60) }
    }

    @Test
    fun `achievement should not unlock twice`() {
        // Given - Başarım zaten açılmış
        every { mockPrefs.getBoolean("profile_TestUser_achievement_first_game", false) } returns true
        every { mockPrefs.getInt("profile_TestUser_games_played", 0) } returns 1

        // When
        achievementManager.checkFirstGame()

        // Then - Tekrar açılmamalı
        verify(exactly = 0) { mockEditor.putBoolean("profile_TestUser_achievement_first_game", true) }
    }

    @Test
    fun `AchievementData should contain all 10 achievements`() {
        // Then
        assertThat(AchievementData.achievements).hasSize(10)
        
        val achievementIds = AchievementData.achievements.map { it.id }
        assertThat(achievementIds).containsExactly(
            "first_game",
            "first_level",
            "questions_25",
            "questions_100",
            "streak_5",
            "level_no_joker",
            "reach_level_5",
            "reach_level_8",
            "games_10",
            "perfect_game"
        )
    }
}
