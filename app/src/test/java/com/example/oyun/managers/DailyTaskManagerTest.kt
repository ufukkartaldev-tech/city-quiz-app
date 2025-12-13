package com.example.oyun.managers

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * DailyTaskManager için unit testler
 * Günlük görev sisteminin doğru çalıştığını test eder
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class DailyTaskManagerTest {

    private lateinit var dailyTaskManager: DailyTaskManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putLong(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockPrefs.getString("last_active_user", "Misafir") } returns "TestUser"
        every { mockPrefs.getInt(any(), 0) } returns 0
        every { mockPrefs.getLong(any(), 0L) } returns System.currentTimeMillis()

        dailyTaskManager = DailyTaskManager(mockContext, mockPrefs)
    }

    @Test
    fun `onCorrectAnswer should increment correct answer count`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_daily_correct_answers", 0) } returns 5

        // When
        dailyTaskManager.onCorrectAnswer()

        // Then
        verify { mockEditor.putInt("profile_TestUser_daily_correct_answers", 6) }
    }

    @Test
    fun `onGameCompleted should increment games completed count`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_daily_games_completed", 0) } returns 2

        // When
        dailyTaskManager.onGameCompleted()

        // Then
        verify { mockEditor.putInt("profile_TestUser_daily_games_completed", 3) }
    }

    @Test
    fun `onCorrectStreak should track highest streak`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_daily_max_streak", 0) } returns 2

        // When
        dailyTaskManager.onCorrectStreak(5)

        // Then
        verify { mockEditor.putInt("profile_TestUser_daily_max_streak", 5) }
    }

    @Test
    fun `onCorrectStreak should not update if current streak is lower`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_daily_max_streak", 0) } returns 10

        // When
        dailyTaskManager.onCorrectStreak(5)

        // Then
        verify(exactly = 0) { mockEditor.putInt("profile_TestUser_daily_max_streak", 5) }
    }

    @Test
    fun `resetDailyProgress should reset all counters`() {
        // When
        dailyTaskManager.resetDailyProgress()

        // Then
        verify { mockEditor.putInt("profile_TestUser_daily_games_completed", 0) }
        verify { mockEditor.putInt("profile_TestUser_daily_correct_answers", 0) }
        verify { mockEditor.putInt("profile_TestUser_daily_max_streak", 0) }
        verify { mockEditor.putLong("profile_TestUser_daily_last_reset", any()) }
    }
}
