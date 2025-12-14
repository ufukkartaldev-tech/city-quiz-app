package com.example.oyun.managers

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import io.mockk.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * ProfileManager Unit Tests
 * 
 * Test Coverage:
 * - Profile creation/selection
 * - Score management
 * - Level progression
 * - Joker management
 * - Preferences persistence
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class ProfileManagerTest {

    private lateinit var profileManager: ProfileManager
    private lateinit var mockContext: Context
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    private val testProfileId = 1

    @Before
    fun setup() {
        mockContext = ApplicationProvider.getApplicationContext()
        
        // Mock SharedPreferences
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockEditor.commit() } returns true
        
        every { mockContext.getSharedPreferences(any(), any()) } returns mockPrefs
        
        // Default values
        every { mockPrefs.getInt(any(), any()) } returns 0
        every { mockPrefs.getString(any(), any()) } returns ""

        profileManager = ProfileManager(mockContext)
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    // ============================================
    // PROFILE MANAGEMENT TESTS
    // ============================================

    @Test
    fun `createProfile should save profile data`() {
        // Given
        val profileName = "Test Player"

        // When
        profileManager.createProfile(testProfileId, profileName)

        // Then
        verify { mockEditor.putString("profile_${testProfileId}_name", profileName) }
        verify { mockEditor.putInt("profile_${testProfileId}_score", 0) }
        verify { mockEditor.putInt("profile_${testProfileId}_current_level", 1) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `selectProfile should set current profile`() {
        // Given
        every { mockPrefs.getString("profile_${testProfileId}_name", "") } returns "Test Player"

        // When
        profileManager.selectProfile(testProfileId)

        // Then
        verify { mockEditor.putInt("current_profile_id", testProfileId) }
    }

    @Test
    fun `getCurrentProfileId should return selected profile`() {
        // Given
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId

        // When
        val profileId = profileManager.getCurrentProfileId()

        // Then
        assertEquals(testProfileId, profileId)
    }

    // ============================================
    // SCORE MANAGEMENT TESTS
    // ============================================

    @Test
    fun `updateScore should save new score`() {
        // Given
        val newScore = 1000
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId

        // When
        profileManager.updateScore(newScore)

        // Then
        verify { mockEditor.putInt("profile_${testProfileId}_score", newScore) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getScore should return current score`() {
        // Given
        val expectedScore = 500
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId
        every { mockPrefs.getInt("profile_${testProfileId}_score", 0) } returns expectedScore

        // When
        val score = profileManager.getScore()

        // Then
        assertEquals(expectedScore, score)
    }

    // ============================================
    // LEVEL MANAGEMENT TESTS
    // ============================================

    @Test
    fun `updateLevel should save new level`() {
        // Given
        val newLevel = 5
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId

        // When
        profileManager.updateLevel(newLevel)

        // Then
        verify { mockEditor.putInt("profile_${testProfileId}_current_level", newLevel) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `getCurrentLevel should return current level`() {
        // Given
        val expectedLevel = 3
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId
        every { mockPrefs.getInt("profile_${testProfileId}_current_level", 1) } returns expectedLevel

        // When
        val level = profileManager.getCurrentLevel()

        // Then
        assertEquals(expectedLevel, level)
    }

    // ============================================
    // JOKER MANAGEMENT TESTS
    // ============================================

    @Test
    fun `getJokerCount should return joker count for type`() {
        // Given
        val jokerType = "fifty_fifty"
        val expectedCount = 3
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId
        every { mockPrefs.getInt("profile_${testProfileId}_joker_$jokerType", 3) } returns expectedCount

        // When
        val count = profileManager.getJokerCount(jokerType)

        // Then
        assertEquals(expectedCount, count)
    }

    @Test
    fun `useJoker should decrease joker count`() {
        // Given
        val jokerType = "fifty_fifty"
        val currentCount = 3
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId
        every { mockPrefs.getInt("profile_${testProfileId}_joker_$jokerType", 3) } returns currentCount

        // When
        val success = profileManager.useJoker(jokerType)

        // Then
        assertTrue(success)
        verify { mockEditor.putInt("profile_${testProfileId}_joker_$jokerType", currentCount - 1) }
    }

    @Test
    fun `useJoker should return false when no jokers left`() {
        // Given
        val jokerType = "fifty_fifty"
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId
        every { mockPrefs.getInt("profile_${testProfileId}_joker_$jokerType", 3) } returns 0

        // When
        val success = profileManager.useJoker(jokerType)

        // Then
        assertFalse(success)
        verify(exactly = 0) { mockEditor.putInt(any(), any()) }
    }

    @Test
    fun `addJoker should increase joker count`() {
        // Given
        val jokerType = "skip_question"
        val currentCount = 2
        every { mockPrefs.getInt("current_profile_id", 1) } returns testProfileId
        every { mockPrefs.getInt("profile_${testProfileId}_joker_$jokerType", 3) } returns currentCount

        // When
        profileManager.addJoker(jokerType)

        // Then
        verify { mockEditor.putInt("profile_${testProfileId}_joker_$jokerType", currentCount + 1) }
    }

    // ============================================
    // PROFILE EXISTENCE TESTS
    // ============================================

    @Test
    fun `profileExists should return true when profile exists`() {
        // Given
        every { mockPrefs.getString("profile_${testProfileId}_name", "") } returns "Test Player"

        // When
        val exists = profileManager.profileExists(testProfileId)

        // Then
        assertTrue(exists)
    }

    @Test
    fun `profileExists should return false when profile does not exist`() {
        // Given
        every { mockPrefs.getString("profile_${testProfileId}_name", "") } returns ""

        // When
        val exists = profileManager.profileExists(testProfileId)

        // Then
        assertFalse(exists)
    }
}
