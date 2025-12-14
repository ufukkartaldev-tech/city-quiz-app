package com.example.oyun.managers

import android.os.CountDownTimer
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * TimeManager Unit Tests
 * 
 * Test Coverage:
 * - Countdown timer
 * - Pause/Resume
 * - Time up events
 * - Timer state management
 */
@ExperimentalCoroutinesApi
class TimeManagerTest {

    private lateinit var timeManager: TimeManager
    private var onTickCalled = false
    private var onFinishCalled = false
    private var lastTickValue = 0L

    @Before
    fun setup() {
        onTickCalled = false
        onFinishCalled = false
        lastTickValue = 0L

        timeManager = TimeManager(
            onTick = { millisUntilFinished ->
                onTickCalled = true
                lastTickValue = millisUntilFinished
            },
            onFinish = {
                onFinishCalled = true
            }
        )
    }

    @After
    fun tearDown() {
        timeManager.cancel()
    }

    // ============================================
    // COUNTDOWN TIMER TESTS
    // ============================================

    @Test
    fun `startTimer should start countdown`() = runTest {
        // Given
        val durationSeconds = 30

        // When
        timeManager.startTimer(durationSeconds)

        // Then
        assertTrue(timeManager.isRunning())
    }

    @Test
    fun `startTimer should call onTick callback`() = runTest {
        // Given
        val durationSeconds = 2
        
        // When
        timeManager.startTimer(durationSeconds)
        Thread.sleep(1100) // Wait for at least one tick

        // Then
        assertTrue(onTickCalled, "onTick should be called")
        assertTrue(lastTickValue > 0, "Tick value should be positive")
    }

    @Test
    fun `timer should call onFinish when time is up`() = runTest {
        // Given
        val durationSeconds = 1

        // When
        timeManager.startTimer(durationSeconds)
        Thread.sleep(1200) // Wait for timer to finish

        // Then
        assertTrue(onFinishCalled, "onFinish should be called")
        assertFalse(timeManager.isRunning(), "Timer should not be running")
    }

    // ============================================
    // PAUSE/RESUME TESTS
    // ============================================

    @Test
    fun `pauseTimer should stop the timer`() = runTest {
        // Given
        timeManager.startTimer(30)
        assertTrue(timeManager.isRunning())

        // When
        timeManager.pauseTimer()

        // Then
        assertFalse(timeManager.isRunning())
    }

    @Test
    fun `resumeTimer should restart with remaining time`() = runTest {
        // Given
        timeManager.startTimer(30)
        Thread.sleep(1100) // Let it run for 1 second
        val timeBeforePause = timeManager.getRemainingTime()
        
        // When
        timeManager.pauseTimer()
        Thread.sleep(500) // Wait while paused
        timeManager.resumeTimer()

        // Then
        assertTrue(timeManager.isRunning())
        // Remaining time should be approximately the same as before pause
        val timeAfterResume = timeManager.getRemainingTime()
        assertTrue(Math.abs(timeBeforePause - timeAfterResume) < 2000) // Within 2 seconds tolerance
    }

    // ============================================
    // CANCEL TESTS
    // ============================================

    @Test
    fun `cancel should stop the timer`() = runTest {
        // Given
        timeManager.startTimer(30)
        assertTrue(timeManager.isRunning())

        // When
        timeManager.cancel()

        // Then
        assertFalse(timeManager.isRunning())
    }

    // ============================================
    // REMAINING TIME TESTS
    // ============================================

    @Test
    fun `getRemainingTime should return correct value`() = runTest {
        // Given
        val durationSeconds = 30
        timeManager.startTimer(durationSeconds)
        Thread.sleep(100) // Small delay

        // When
        val remainingTime = timeManager.getRemainingTime()

        // Then
        assertTrue(remainingTime > 0)
        assertTrue(remainingTime <= durationSeconds * 1000L)
    }
}
