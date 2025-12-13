package com.example.oyun.utils

import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import android.view.View

/**
 * Kullanıcı geri bildirimi için yardımcı sınıf
 */
class FeedbackManager(private val context: Context) {
    
    private val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    
    /**
     * Başarı geri bildirimi gösterir
     */
    fun showSuccessFeedback(view: View, message: String, withVibration: Boolean = true) {
        // Snackbar göster
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(context.getColor(android.R.color.holo_green_dark))
        snackbar.setTextColor(context.getColor(android.R.color.white))
        snackbar.show()
        
        // Titreşim
        if (withVibration && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
        }
    }
    
    /**
     * Hata geri bildirimi gösterir
     */
    fun showErrorFeedback(view: View, message: String, withVibration: Boolean = true) {
        // Snackbar göster
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(context.getColor(android.R.color.holo_red_dark))
        snackbar.setTextColor(context.getColor(android.R.color.white))
        snackbar.show()
        
        // Titreşim (daha uzun)
        if (withVibration && vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
        }
    }
    
    /**
     * Bilgi geri bildirimi gösterir
     */
    fun showInfoFeedback(view: View, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_SHORT)
        snackbar.setBackgroundTint(context.getColor(android.R.color.holo_blue_dark))
        snackbar.setTextColor(context.getColor(android.R.color.white))
        snackbar.show()
    }
    
    /**
     * Streak (üst üste doğru) geri bildirimi
     */
    fun showStreakFeedback(view: View, streak: Int) {
        val message = when {
            streak >= 10 -> "🔥 EFSANE! $streak doğru üst üste!"
            streak >= 5 -> "🔥 ATEŞ! $streak doğru üst üste!"
            streak >= 3 -> "🔥 Harika! $streak doğru üst üste!"
            else -> return
        }
        
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        snackbar.setBackgroundTint(context.getColor(android.R.color.holo_orange_dark))
        snackbar.setTextColor(context.getColor(android.R.color.white))
        snackbar.show()
        
        // Özel titreşim
        if (vibrator.hasVibrator()) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 50, 50, 50, 50, 100), -1))
        }
    }
    
    /**
     * Pozitif mesajlar listesi
     */
    fun getRandomSuccessMessage(): String {
        val messages = listOf(
            "Harika! ✨",
            "Mükemmel! 🌟",
            "Süpersin! 🎉",
            "Bravo! 👏",
            "Aferin! 🎯",
            "Muhteşem! 💫",
            "Tebrikler! 🏆",
            "Şahane! ⭐"
        )
        return messages.random()
    }
    
    /**
     * Motivasyon mesajları (yanlış cevap sonrası)
     */
    fun getMotivationMessage(): String {
        val messages = listOf(
            "Sorun değil, devam et! 💪",
            "Bir dahaki sefere! 🎯",
            "Pes etme! 🌟",
            "Denemeye devam! ✨",
            "Başarısızlık başarının anahtarıdır! 🔑"
        )
        return messages.random()
    }
}
