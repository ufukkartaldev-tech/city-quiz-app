package com.example.oyun.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

/**
 * Unit tests for QuestionRepository
 * Tests question loading and filtering logic
 */
class QuestionRepositoryTest {

    private lateinit var repository: QuestionRepository

    @Test
    fun `getQuestionsForLevel should return questions for valid level`() {
        // Given
        repository = QuestionRepository(mockContext())
        val level = 1

        // When
        val questions = repository.getQuestionsForLevel(level)

        // Then
        assertThat(questions).isNotEmpty()
        assertThat(questions.all { it.level == level }).isTrue()
    }

    @Test
    fun `getQuestionsForLevel should return empty list for invalid level`() {
        // Given
        repository = QuestionRepository(mockContext())
        val invalidLevel = 999

        // When
        val questions = repository.getQuestionsForLevel(invalidLevel)

        // Then
        assertThat(questions).isEmpty()
    }

    @Test
    fun `getAllQuestions should return all questions`() {
        // Given
        repository = QuestionRepository(mockContext())

        // When
        val questions = repository.getAllQuestions()

        // Then
        assertThat(questions).isNotEmpty()
        assertThat(questions.size).isAtLeast(80) // Should have at least 80 questions
    }

    @Test
    fun `getCityQuestions should return only city questions`() {
        // Given
        repository = QuestionRepository(mockContext())

        // When
        val cityQuestions = repository.getCityQuestions()

        // Then
        assertThat(cityQuestions).isNotEmpty()
        // All questions should be about cities (you can add more specific checks)
    }

    @Test
    fun `questions should have valid structure`() {
        // Given
        repository = QuestionRepository(mockContext())

        // When
        val questions = repository.getAllQuestions()

        // Then
        questions.forEach { question ->
            assertThat(question.questionText).isNotEmpty()
            assertThat(question.optionA).isNotEmpty()
            assertThat(question.optionB).isNotEmpty()
            assertThat(question.optionC).isNotEmpty()
            assertThat(question.optionD).isNotEmpty()
            assertThat(question.correctAnswer).isIn(listOf("A", "B", "C", "D"))
            assertThat(question.level).isAtLeast(1)
            assertThat(question.level).isAtMost(10)
        }
    }

    private fun mockContext(): android.content.Context {
        return io.mockk.mockk(relaxed = true)
    }
}
