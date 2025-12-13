package com.example.oyun.managers

import android.os.CountDownTimer

class TimeManager(private val uiManager: UIManager) {
    private var timer: CountDownTimer? = null
    private var isPaused = false
    private var remainingTime: Long = 0

    fun startTimer(durationInMillis: Long, onFinish: (Boolean) -> Unit) {
        remainingTime = durationInMillis
        isPaused = false
        timer?.cancel()

        timer = object : CountDownTimer(durationInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused) {
                    remainingTime = millisUntilFinished
                    uiManager.updateTimer((millisUntilFinished / 1000).toInt())
                }
            }

            override fun onFinish() {
                remainingTime = 0
                uiManager.updateTimer(0)
                if (!isPaused) {
                    onFinish(true)
                }
            }
        }.start()
    }

    fun pauseTimer() {
        if (!isPaused) {
            timer?.cancel()
            isPaused = true
        }
    }

    fun resumeTimer(onFinish: (Boolean) -> Unit) {
        if (isPaused && remainingTime > 0) {
            startTimer(remainingTime, onFinish)
        }
    }

    fun stopTimer() {
        timer?.cancel()
        isPaused = false
        remainingTime = 0
    }
}
