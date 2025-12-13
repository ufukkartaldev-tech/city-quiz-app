package com.example.oyun.ui.game

import com.example.oyun.R

import com.example.oyun.managers.SoundManager
import com.example.oyun.managers.TimeManager

/**
 * Oyun cevap işlemlerini yöneten sınıf
 */
class AnswerHandler(
    private val gameViewModel: GameViewModel,
    private val soundManager: SoundManager,
    private val timerManager: TimeManager,
    private val onAnswerProcessed: () -> Unit
) {

    /**
     * Kullanıcının cevabını işler
     */
    fun processAnswer(isCorrect: Boolean) {
        timerManager.stopTimer()
        // playFeedbackSound(isCorrect) // GameViewModel artık sesi yönetiyor
        gameViewModel.handleAnswer(isCorrect)
        
        // Kısa bir gecikme sonrası sonraki adıma geç
        onAnswerProcessed()
    }

    /**
     * Süre dolduğunda çağrılır
     */
    fun handleTimeUp(correctAnswer: String, onComplete: () -> Unit) {
        processAnswer(false)
        onComplete()
    }
}
