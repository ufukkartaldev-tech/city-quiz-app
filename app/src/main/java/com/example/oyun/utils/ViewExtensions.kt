package com.example.oyun.utils

import android.os.SystemClock
import android.view.View
import android.view.animation.AnimationUtils
import com.example.oyun.R

/**
 * Hızlı çift tıklamaları önlemek için bir görünümün tıklama olayına bir gecikme (debounce) ekler.
 * @param delayMillis Gecikme süresi (varsayılan: 600ms)
 * @param action Tıklama olayında çalıştırılacak blok
 */
fun View.setSafeOnClickListener(delayMillis: Long = 600L, action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            // Son tıklama zamanı ile şu anki zaman arasındaki farkı kontrol et
            if (SystemClock.elapsedRealtime() - lastClickTime < delayMillis) {
                return // Gecikme süresi dolmadıysa işlemi durdur (çift tıklamayı engelle)
            }
            lastClickTime = SystemClock.elapsedRealtime()
            
            // Tıklama animasyonu ekle
            v.playClickAnimation()
            
            action() // İşlemi çalıştır
        }
    })
}

/**
 * Tıklama animasyonu oynatır (scale efekti)
 */
fun View.playClickAnimation() {
    val scaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)
    startAnimation(scaleDown)
}

/**
 * Fade in animasyonu
 */
fun View.fadeIn(duration: Long = 300) {
    alpha = 0f
    visibility = View.VISIBLE
    animate()
        .alpha(1f)
        .setDuration(duration)
        .setListener(null)
}

/**
 * Fade out animasyonu
 */
fun View.fadeOut(duration: Long = 300) {
    animate()
        .alpha(0f)
        .setDuration(duration)
        .withEndAction {
            visibility = View.GONE
        }
}

/**
 * Slide in from bottom animasyonu
 */
fun View.slideInFromBottom(duration: Long = 400) {
    visibility = View.VISIBLE
    translationY = height.toFloat()
    animate()
        .translationY(0f)
        .setDuration(duration)
        .setListener(null)
}

/**
 * Slide out to bottom animasyonu
 */
fun View.slideOutToBottom(duration: Long = 400) {
    animate()
        .translationY(height.toFloat())
        .setDuration(duration)
        .withEndAction {
            visibility = View.GONE
        }
}

/**
 * Shake animasyonu (yanlış cevap için)
 */
fun View.shake() {
    val shake = AnimationUtils.loadAnimation(context, R.anim.shake)
    startAnimation(shake)
}

/**
 * Pulse animasyonu (başarı için)
 */
fun View.pulse() {
    val pulse = AnimationUtils.loadAnimation(context, R.anim.pulse)
    startAnimation(pulse)
}
