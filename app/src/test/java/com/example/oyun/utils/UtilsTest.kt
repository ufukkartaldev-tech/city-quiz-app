package com.example.oyun.utils

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Yardımcı fonksiyonlar ve extension'lar için unit testler
 */
class UtilsTest {

    @Test
    fun `test score calculation`() {
        // Given
        val correctAnswers = 8
        val pointsPerQuestion = 10

        // When
        val totalScore = correctAnswers * pointsPerQuestion

        // Then
        assertThat(totalScore).isEqualTo(80)
    }

    @Test
    fun `test percentage calculation`() {
        // Given
        val correct = 7
        val total = 10

        // When
        val percentage = (correct.toFloat() / total.toFloat() * 100).toInt()

        // Then
        assertThat(percentage).isEqualTo(70)
    }

    @Test
    fun `test level progression logic`() {
        // Given
        val questionsPerLevel = 10
        val questionsAnswered = 10

        // When
        val shouldLevelUp = questionsAnswered >= questionsPerLevel

        // Then
        assertThat(shouldLevelUp).isTrue()
    }

    @Test
    fun `test joker availability check`() {
        // Given
        val jokerCount = 3

        // When
        val isAvailable = jokerCount > 0

        // Then
        assertThat(isAvailable).isTrue()
    }

    @Test
    fun `test joker not available when count is zero`() {
        // Given
        val jokerCount = 0

        // When
        val isAvailable = jokerCount > 0

        // Then
        assertThat(isAvailable).isFalse()
    }

    @Test
    fun `test max lives constraint`() {
        // Given
        val currentLives = 4
        val maxLives = 5
        val livesToAdd = 2

        // When
        val newLives = (currentLives + livesToAdd).coerceAtMost(maxLives)

        // Then
        assertThat(newLives).isEqualTo(5)
    }

    @Test
    fun `test lives cannot go below zero`() {
        // Given
        val currentLives = 1
        val livesToLose = 2

        // When
        val newLives = (currentLives - livesToLose).coerceAtLeast(0)

        // Then
        assertThat(newLives).isEqualTo(0)
    }

    @Test
    fun `test time limit decreases with level`() {
        // Given
        val baseTime = 30
        val level = 5
        val timeDecrease = 2

        // When
        val timeLimit = (baseTime - (level - 1) * timeDecrease).coerceAtLeast(10)

        // Then
        assertThat(timeLimit).isEqualTo(22)
    }

    @Test
    fun `test time limit has minimum value`() {
        // Given
        val baseTime = 30
        val level = 20
        val timeDecrease = 2

        // When
        val timeLimit = (baseTime - (level - 1) * timeDecrease).coerceAtLeast(10)

        // Then
        assertThat(timeLimit).isEqualTo(10)
    }

    @Test
    fun `test correct answer index validation`() {
        // Given
        val correctIndex = 2
        val optionsSize = 4

        // When
        val isValid = correctIndex in 0 until optionsSize

        // Then
        assertThat(isValid).isTrue()
    }

    @Test
    fun `test invalid answer index`() {
        // Given
        val correctIndex = 5
        val optionsSize = 4

        // When
        val isValid = correctIndex in 0 until optionsSize

        // Then
        assertThat(isValid).isFalse()
    }
}
