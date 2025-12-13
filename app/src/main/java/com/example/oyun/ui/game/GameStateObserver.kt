package com.example.oyun.ui.game

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.oyun.data.Question

import com.example.oyun.managers.TimeManager
import com.example.oyun.managers.UIManager
import kotlinx.coroutines.launch

/**
 * GameViewModel state değişikliklerini gözlemleyen sınıf
 */
class GameStateObserver(
    private val lifecycleOwner: LifecycleOwner,
    private val gameViewModel: GameViewModel,
    private val uiManager: UIManager,
    private val timerManager: TimeManager,
    private val maxLives: Int,
    private val onGameOver: () -> Unit,
    private val onLevelUp: (Int) -> Unit,
    private val onAnswerRequired: (Question, Int) -> Unit,
    private val onTimeUp: () -> Unit
) {

    fun observe() {
        observeGameState()
        observeGameEvents()
    }

    private fun observeGameState() {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                gameViewModel.uiState.collect { state ->
                    // UI güncellemeleri
                    uiManager.updateScore(state.score)
                    uiManager.updateLives(state.lives, maxLives)
                    uiManager.updateLevel(state.currentLevel)
                    uiManager.updateJokerInfo(
                        state.jokerInventory.fiftyFifty,
                        state.jokerInventory.skip,
                        state.jokerInventory.gainLife
                    )

                    // Yeni soru göster
                    state.currentQuestion?.let { question ->
                        if (!state.isAnswered) {
                            displayQuestion(question)
                        }
                    }
                }
            }
        }
    }

    private fun displayQuestion(question: Question) {
        uiManager.setQuestion(question)
        uiManager.displayAnswers(question.options)
        
        // Cevap butonlarını ayarla
        uiManager.setAnswerButtonListeners(question.correctAnswerIndex) { selectedIndex ->
            onAnswerRequired(question, selectedIndex)
        }
        
        // Zamanlayıcıyı başlat
        timerManager.startTimer(QUESTION_TIME_LIMIT) { timeIsUp ->
            if (timeIsUp) {
                uiManager.showTimeUpFeedback(question.options[question.correctAnswerIndex])
                onTimeUp()
            }
        }
    }

    private fun observeGameEvents() {
        lifecycleOwner.lifecycleScope.launch {
            lifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Oyun bitti eventi
                launch {
                    gameViewModel.gameOverEvent.collect { event ->
                        event?.getContentIfNotHandled()?.let {
                            onGameOver()
                        }
                    }
                }
                
                // Level atlama eventi
                launch {
                    gameViewModel.levelUpEvent.collect { event ->
                        event?.getContentIfNotHandled()?.let { newLevel ->
                            onLevelUp(newLevel)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private const val QUESTION_TIME_LIMIT = 30 * 1000L // 30 saniye
    }
}
