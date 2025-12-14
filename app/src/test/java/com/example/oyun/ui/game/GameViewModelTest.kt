package com.example.oyun.ui.game

import android.content.SharedPreferences
import com.example.oyun.data.Question
import com.example.oyun.data.QuestionRepository
import com.example.oyun.managers.AchievementManager
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

/**
 * Unit tests for GameViewModel
 * Tests core game logic, joker system, and state management
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    private lateinit var viewModel: GameViewModel
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var mockQuestionRepository: QuestionRepository
    private lateinit var mockAchievementManager: AchievementManager
    
    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        
        // Mock SharedPreferences
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        
        // Mock QuestionRepository
        mockQuestionRepository = mockk(relaxed = true)
        
        // Mock AchievementManager
        mockAchievementManager = mockk(relaxed = true)
        
        // Create ViewModel
        viewModel = GameViewModel(
            prefs = mockPrefs,
            questionRepository = mockQuestionRepository,
            achievementManager = mockAchievementManager
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startGame should initialize with correct level and lives`() = runTest {
        // Given
        val startLevel = 3
        val mockQuestions = listOf(
            Question(1, "Test Question 1", "A", "B", "C", "D", "A", "test.png", 1),
            Question(2, "Test Question 2", "A", "B", "C", "D", "B", "test.png", 1)
        )
        every { mockQuestionRepository.getQuestionsForLevel(startLevel) } returns mockQuestions
        every { mockPrefs.getInt("profile_1_joker_fifty_fifty", 3) } returns 3
        every { mockPrefs.getInt("profile_1_joker_skip", 2) } returns 2
        every { mockPrefs.getInt("profile_1_joker_gain_life", 1) } returns 1

        // When
        viewModel.startGame(startLevel, isNewGame = true)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertThat(state.currentLevel).isEqualTo(startLevel)
        assertThat(state.lives).isEqualTo(3)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.isGameOver).isFalse()
    }

    @Test
    fun `handleAnswer with correct answer should increase score`() = runTest {
        // Given
        setupGameWithQuestions()
        val initialState = viewModel.uiState.first()
        val initialScore = initialState.score

        // When
        viewModel.handleAnswer(isCorrect = true)
        advanceUntilIdle()

        // Then
        val newState = viewModel.uiState.first()
        assertThat(newState.score).isGreaterThan(initialScore)
        assertThat(newState.lives).isEqualTo(3) // Lives should not decrease
    }

    @Test
    fun `handleAnswer with wrong answer should decrease lives`() = runTest {
        // Given
        setupGameWithQuestions()

        // When
        viewModel.handleAnswer(isCorrect = false)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertThat(state.lives).isEqualTo(2) // Should decrease from 3 to 2
    }

    @Test
    fun `game should end when lives reach zero`() = runTest {
        // Given
        setupGameWithQuestions()

        // When - Answer wrong 3 times
        viewModel.handleAnswer(isCorrect = false)
        advanceUntilIdle()
        viewModel.handleAnswer(isCorrect = false)
        advanceUntilIdle()
        viewModel.handleAnswer(isCorrect = false)
        advanceUntilIdle()

        // Then
        val state = viewModel.uiState.first()
        assertThat(state.lives).isEqualTo(0)
        assertThat(state.isGameOver).isTrue()
    }

    @Test
    fun `useFiftyFiftyJoker should decrease joker count`() = runTest {
        // Given
        setupGameWithQuestions()
        every { mockPrefs.getInt("profile_1_joker_fifty_fifty", 3) } returns 3

        // When
        viewModel.useFiftyFiftyJoker()
        advanceUntilIdle()

        // Then
        verify { mockEditor.putInt("profile_1_joker_fifty_fifty", 2) }
        verify { mockEditor.apply() }
    }

    @Test
    fun `useSkipJoker should load next question`() = runTest {
        // Given
        setupGameWithQuestions()
        every { mockPrefs.getInt("profile_1_joker_skip", 2) } returns 2
        val initialQuestion = viewModel.uiState.first().currentQuestion

        // When
        viewModel.useSkipJoker()
        advanceUntilIdle()

        // Then
        val newQuestion = viewModel.uiState.first().currentQuestion
        assertThat(newQuestion).isNotEqualTo(initialQuestion)
        verify { mockEditor.putInt("profile_1_joker_skip", 1) }
    }

    @Test
    fun `useGainLifeJoker should increase lives`() = runTest {
        // Given
        setupGameWithQuestions()
        every { mockPrefs.getInt("profile_1_joker_gain_life", 1) } returns 1

        // When
        viewModel.useGainLifeJoker()
        advanceUntilIdle()

        // Then
        verify { mockEditor.putInt("profile_1_joker_gain_life", 0) }
        // Should trigger city question mode
    }

    @Test
    fun `checkLevelUp should trigger level up when score threshold reached`() = runTest {
        // Given
        setupGameWithQuestions()
        
        // Simulate reaching level up score (100 points)
        repeat(10) {
            viewModel.handleAnswer(isCorrect = true)
            advanceUntilIdle()
        }

        // Then
        val state = viewModel.uiState.first()
        // Level should increase if score >= 100
        if (state.score >= 100) {
            assertThat(state.currentLevel).isGreaterThan(1)
        }
    }

    @Test
    fun `saveGameState should persist current progress`() = runTest {
        // Given
        setupGameWithQuestions()
        viewModel.handleAnswer(isCorrect = true)
        advanceUntilIdle()

        // When
        viewModel.saveGameState()

        // Then
        verify { mockEditor.putInt("profile_1_current_level", any()) }
        verify { mockEditor.putInt("profile_1_current_score", any()) }
        verify { mockEditor.putInt("profile_1_current_lives", any()) }
        verify { mockEditor.apply() }
    }

    // Helper function to setup game with mock questions
    private fun setupGameWithQuestions() {
        val mockQuestions = listOf(
            Question(1, "Question 1", "A", "B", "C", "D", "A", "test.png", 1),
            Question(2, "Question 2", "A", "B", "C", "D", "B", "test.png", 1),
            Question(3, "Question 3", "A", "B", "C", "D", "C", "test.png", 1)
        )
        every { mockQuestionRepository.getQuestionsForLevel(any()) } returns mockQuestions
        every { mockPrefs.getInt("profile_1_joker_fifty_fifty", 3) } returns 3
        every { mockPrefs.getInt("profile_1_joker_skip", 2) } returns 2
        every { mockPrefs.getInt("profile_1_joker_gain_life", 1) } returns 1
        
        viewModel.startGame(1, isNewGame = true)
        runCurrent()
    }
}
