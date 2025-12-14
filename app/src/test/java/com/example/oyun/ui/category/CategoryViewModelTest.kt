package com.example.oyun.ui.category

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.oyun.data.HybridQuestionRepository
import com.example.oyun.data.Question
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * CategoryViewModel Unit Tests
 * 
 * Test Coverage:
 * - Category loading
 * - Question counting
 * - Category statistics
 * - Lock status
 */
@ExperimentalCoroutinesApi
class CategoryViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: CategoryViewModel
    private lateinit var mockRepository: HybridQuestionRepository
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        mockRepository = mockk(relaxed = true)
        viewModel = CategoryViewModel(mockRepository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ============================================
    // CATEGORY LOADING TESTS
    // ============================================

    @Test
    fun `loadCategories should load 7 categories`() = runTest {
        // Given
        val questions = createMockQuestions()
        coEvery { mockRepository.getAllQuestions() } returns questions

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categories = viewModel.categories.first()
        assertEquals(7, categories.size, "Should have 7 categories")
    }

    @Test
    fun `loadCategories should calculate question counts correctly`() = runTest {
        // Given
        val questions = listOf(
            createQuestion(1, "GEOGRAPHY"),
            createQuestion(2, "GEOGRAPHY"),
            createQuestion(3, "HISTORY"),
            createQuestion(4, "CULTURE")
        )
        coEvery { mockRepository.getAllQuestions() } returns questions

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categories = viewModel.categories.first()
        val geography = categories.find { it.code == "GEOGRAPHY" }
        val history = categories.find { it.code == "HISTORY" }
        val culture = categories.find { it.code == "CULTURE" }

        assertEquals(2, geography?.questionCount)
        assertEquals(1, history?.questionCount)
        assertEquals(1, culture?.questionCount)
    }

    @Test
    fun `loadCategories should set correct category names`() = runTest {
        // Given
        coEvery { mockRepository.getAllQuestions() } returns emptyList()

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categories = viewModel.categories.first()
        val categoryNames = categories.map { it.name }

        assertTrue(categoryNames.contains("Coğrafya"))
        assertTrue(categoryNames.contains("Tarih"))
        assertTrue(categoryNames.contains("Kültür"))
        assertTrue(categoryNames.contains("Spor"))
    }

    @Test
    fun `loadCategories should set correct category icons`() = runTest {
        // Given
        coEvery { mockRepository.getAllQuestions() } returns emptyList()

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categories = viewModel.categories.first()
        val geography = categories.find { it.code == "GEOGRAPHY" }
        val history = categories.find { it.code == "HISTORY" }

        assertEquals("🌍", geography?.icon)
        assertEquals("📜", history?.icon)
    }

    // ============================================
    // LOCK STATUS TESTS
    // ============================================

    @Test
    fun `categories with no questions should be locked`() = runTest {
        // Given - Bilim kategorisinde soru yok
        val questions = listOf(
            createQuestion(1, "GEOGRAPHY"),
            createQuestion(2, "HISTORY")
        )
        coEvery { mockRepository.getAllQuestions() } returns questions

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categories = viewModel.categories.first()
        val science = categories.find { it.code == "SCIENCE" }

        assertTrue(science?.isLocked == true, "Science should be locked when no questions")
        assertEquals(10, science?.requiredLevel)
    }

    @Test
    fun `categories with questions should not be locked`() = runTest {
        // Given
        val questions = listOf(
            createQuestion(1, "GEOGRAPHY"),
            createQuestion(2, "SCIENCE")
        )
        coEvery { mockRepository.getAllQuestions() } returns questions

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val categories = viewModel.categories.first()
        val geography = categories.find { it.code == "GEOGRAPHY" }
        val science = categories.find { it.code == "SCIENCE" }

        assertFalse(geography?.isLocked == true)
        assertFalse(science?.isLocked == true)
    }

    // ============================================
    // STATISTICS TESTS
    // ============================================

    @Test
    fun `categoryStats should contain correct counts`() = runTest {
        // Given
        val questions = listOf(
            createQuestion(1, "GEOGRAPHY"),
            createQuestion(2, "GEOGRAPHY"),
            createQuestion(3, "HISTORY")
        )
        coEvery { mockRepository.getAllQuestions() } returns questions

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val stats = viewModel.categoryStats.first()
        assertEquals(2, stats["GEOGRAPHY"])
        assertEquals(1, stats["HISTORY"])
    }

    @Test
    fun `getQuestionsForCategory should return correct count`() = runTest {
        // Given
        val category = "GEOGRAPHY"
        val questions = listOf(
            createQuestion(1, category),
            createQuestion(2, category)
        )
        coEvery { mockRepository.getQuestionsByCategory(category) } returns questions

        // When
        val count = viewModel.getQuestionsForCategory(category)

        // Then
        assertEquals(2, count)
        coVerify { mockRepository.getQuestionsByCategory(category) }
    }

    // ============================================
    // ERROR HANDLING TESTS
    // ============================================

    @Test
    fun `loadCategories should handle repository error gracefully`() = runTest {
        // Given
        coEvery { mockRepository.getAllQuestions() } throws Exception("Network error")

        // When
        viewModel.loadCategories()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then - Should not crash, return empty list
        val categories = viewModel.categories.first()
        assertTrue(categories.isEmpty())
    }

    @Test
    fun `getQuestionsForCategory should return 0 on error`() = runTest {
        // Given
        coEvery { mockRepository.getQuestionsByCategory(any()) } throws Exception("Error")

        // When
        val count = viewModel.getQuestionsForCategory("GEOGRAPHY")

        // Then
        assertEquals(0, count)
    }

    // ============================================
    // HELPER METHODS
    // ============================================

    private fun createMockQuestions(): List<Question> {
        return listOf(
            createQuestion(1, "GEOGRAPHY"),
            createQuestion(2, "HISTORY"),
            createQuestion(3, "CULTURE"),
            createQuestion(4, "SPORTS"),
            createQuestion(5, "GENERAL"),
            createQuestion(6, "SCIENCE"),
            createQuestion(7, "ART")
        )
    }

    private fun createQuestion(id: Int, category: String): Question {
        return Question(
            id = id,
            questionText = "Test Question $id",
            optionA = "A",
            optionB = "B",
            optionC = "C",
            optionD = "D",
            correctAnswer = "A",
            imageName = "test.png",
            level = 1,
            category = category
        )
    }
}
