package com.example.oyun.domain

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.oyun.data.Question
import com.example.oyun.data.QuestionRepository
import com.example.oyun.managers.AchievementManager
import com.example.oyun.managers.DailyTaskManager
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import android.content.SharedPreferences

/**
 * GameViewModel için unit testler
 * ViewModel'in oyun mantığını, state yönetimini ve joker kullanımını test eder
 */
@OptIn(ExperimentalCoroutinesApi::class)
class GameViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: GameViewModel
    private lateinit var mockRepository: QuestionRepository
    private lateinit var mockAchievementManager: AchievementManager
    private lateinit var mockDailyTaskManager: DailyTaskManager
    private lateinit var mockPrefs: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Mock nesnelerini oluştur
        mockRepository = mockk(relaxed = true)
        mockAchievementManager = mockk(relaxed = true)
        mockDailyTaskManager = mockk(relaxed = true)
        mockPrefs = mockk(relaxed = true)
        mockEditor = mockk(relaxed = true)

        // SharedPreferences mock davranışları
        every { mockPrefs.edit() } returns mockEditor
        every { mockEditor.putInt(any(), any()) } returns mockEditor
        every { mockEditor.apply() } just Runs
        every { mockPrefs.getString("last_active_user", any()) } returns "TestUser"
        every { mockPrefs.getInt(any(), any()) } returns 0

        viewModel = GameViewModel(
            mockRepository,
            mockAchievementManager,
            mockDailyTaskManager,
            mockPrefs
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `startGame should initialize game state correctly`() {
        // Given
        val startLevel = 1
        val testQuestion = Question(
            questionText = "Test soru?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion

        // When
        viewModel.startGame(startLevel)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.currentLevel).isEqualTo(1)
        assertThat(state.score).isEqualTo(0)
        assertThat(state.lives).isEqualTo(3)
        assertThat(state.questionsAnsweredInLevel).isEqualTo(0)
        assertThat(state.isLoading).isFalse()
        verify { mockRepository.loadQuestionsForLevel(startLevel) }
    }

    @Test
    fun `handleAnswer with correct answer should increase score`() {
        // Given
        val testQuestion = Question(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialScore = viewModel.uiState.value.score

        // When
        viewModel.handleAnswer(isCorrect = true)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.score).isEqualTo(initialScore + 10)
        assertThat(state.lives).isEqualTo(3)
        assertThat(state.questionsAnsweredInLevel).isEqualTo(1)
        verify { mockDailyTaskManager.onCorrectAnswer() }
    }

    @Test
    fun `handleAnswer with wrong answer should decrease lives`() {
        // Given
        val testQuestion = Question(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialLives = viewModel.uiState.value.lives

        // When
        viewModel.handleAnswer(isCorrect = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.lives).isEqualTo(initialLives - 1)
        assertThat(state.questionsAnsweredInLevel).isEqualTo(0)
    }

    @Test
    fun `handleAnswer should trigger game over when lives reach zero`() {
        // Given
        val testQuestion = Question(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - 3 yanlış cevap ver
        viewModel.handleAnswer(isCorrect = false)
        viewModel.handleAnswer(isCorrect = false)
        viewModel.handleAnswer(isCorrect = false)
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.lives).isEqualTo(0)
        assertThat(viewModel.gameOverEvent.value).isNotNull()
    }

    @Test
    fun `useFiftyFiftyJoker should decrease joker count`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_joker_fiftyfifty_count", 0) } returns 3
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        val initialCount = viewModel.uiState.value.jokerInventory.fiftyFifty

        // When
        viewModel.useFiftyFiftyJoker()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.jokerInventory.fiftyFifty).isEqualTo(initialCount - 1)
    }

    @Test
    fun `useSkipJoker should skip question and increase answered count`() {
        // Given
        every { mockPrefs.getInt("profile_TestUser_joker_skip_count", 0) } returns 2
        val testQuestion = Question(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.useSkipJoker()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        val state = viewModel.uiState.value
        assertThat(state.jokerInventory.skip).isEqualTo(1)
        assertThat(state.questionsAnsweredInLevel).isEqualTo(1)
        assertThat(state.isAnswered).isTrue()
    }

    @Test
    fun `addLife should increase lives but not exceed max`() {
        // Given
        val testQuestion = Question(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - 5 can ekle (max 5)
        repeat(5) {
            viewModel.addLife()
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.lives).isAtMost(5)
    }

    @Test
    fun `consecutive correct answers should trigger streak achievement`() {
        // Given
        val testQuestion = Question(
            questionText = "Test?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        every { mockRepository.getNextQuestion() } returns testQuestion
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // When - 5 doğru cevap ver
        repeat(5) {
            viewModel.handleAnswer(isCorrect = true)
        }
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        verify(atLeast = 1) { mockAchievementManager.checkStreakAchievement(any()) }
    }

    @Test
    fun `loadNextQuestion should update current question`() {
        // Given
        val question1 = Question(
            questionText = "Soru 1?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 0
        )
        val question2 = Question(
            questionText = "Soru 2?",
            options = listOf("A", "B", "C", "D"),
            correctAnswerIndex = 1
        )
        every { mockRepository.getNextQuestion() } returnsMany listOf(question1, question2)
        viewModel.startGame(1)
        testDispatcher.scheduler.advanceUntilIdle()

        // When
        viewModel.loadNextQuestion()
        testDispatcher.scheduler.advanceUntilIdle()

        // Then
        assertThat(viewModel.uiState.value.currentQuestion?.questionText).isEqualTo("Soru 2?")
        assertThat(viewModel.uiState.value.isAnswered).isFalse()
    }
}
