package com.example.oyun.ui.multiplayer

import android.os.CountDownTimer
import com.example.oyun.databinding.ActivityMultiplayerGameBinding

/**
 * Multiplayer oyun zamanlayıcısını yöneten sınıf
 */
class MultiplayerTimer(
    private val binding: ActivityMultiplayerGameBinding,
    private val timePerQuestion: Long,
    private val onTimeUp: () -> Unit
) {
    
    private var timer: CountDownTimer? = null
    private var timeLeft: Long = timePerQuestion

    fun start() {
        cancel()
        timeLeft = timePerQuestion
        
        timer = object : CountDownTimer(timePerQuestion, TICK_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeft = millisUntilFinished
                binding.timerText.text = "${millisUntilFinished / 1000}"
            }

            override fun onFinish() {
                binding.timerText.text = "0"
                onTimeUp()
            }
        }.start()
    }

    fun cancel() {
        timer?.cancel()
        timer = null
    }

    fun getTimeLeft(): Long = timeLeft

    companion object {
        private const val TICK_INTERVAL = 1000L
    }
}
