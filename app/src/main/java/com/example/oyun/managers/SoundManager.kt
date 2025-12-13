package com.example.oyun.managers

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioManager
import android.media.SoundPool
import android.util.Log
import com.example.oyun.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SoundManager @Inject constructor(@ApplicationContext private val context: Context) {

    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<Int, Int>()
    private val prefs: SharedPreferences = context.getSharedPreferences("quiz_prefs", Context.MODE_PRIVATE)

    // Ses açık/kapalı durumu
    private var isSoundEnabled: Boolean
        get() = prefs.getBoolean("sound_enabled", true)
        set(value) = prefs.edit().putBoolean("sound_enabled", value).apply()

    init {
        // SoundPool'u oluşturma
        @Suppress("DEPRECATION")
        soundPool = SoundPool(10, AudioManager.STREAM_MUSIC, 0)
        Log.d("SoundManager", "SoundPool başlatıldı.")

        loadSounds()
    }

    private fun loadSounds() {
        soundPool?.let { pool ->
            try {
                // Mevcut ses dosyalarını yükle
                soundMap[R.raw.correct_answer_sound] = pool.load(context, R.raw.correct_answer_sound, 1)
                soundMap[R.raw.wrong_answer_sound] = pool.load(context, R.raw.wrong_answer_sound, 1)
                
                // TODO: İleride eklenecek sesler için placeholder
                // soundMap[R.raw.level_up_sound] = pool.load(context, R.raw.level_up_sound, 1)
                
                Log.d("SoundManager", "Ses dosyaları başarıyla yüklendi.")
            } catch (e: Exception) {
                Log.e("SoundManager", "Ses dosyalarını yüklerken hata oluştu: ${e.message}")
            }
        }
    }

    private fun playSound(soundId: Int) {
        if (!isSoundEnabled) return

        soundPool?.let { pool ->
            val loadedSoundId = soundMap[soundId]
            if (loadedSoundId != null) {
                pool.play(loadedSoundId, 1.0f, 1.0f, 0, 0, 1.0f)
            } else {
                Log.e("SoundManager", "Ses dosyası bulunamadı, ID: $soundId")
            }
        }
    }
    
    // --- Oyun İçi Olay Metotları ---
    
    fun playCorrectAnswer() {
        playSound(R.raw.correct_answer_sound)
    }

    fun playWrongAnswer() {
        playSound(R.raw.wrong_answer_sound)
    }

    fun playLevelUp() {
        // Şimdilik doğru cevap sesini (belki biraz farklı parametreyle) kullanabiliriz veya sessiz geçebiliriz
        // Eğer level_up.mp3 eklenirse burayı güncelle
        playSound(R.raw.correct_answer_sound) 
    }

    fun playGameOver() {
        playSound(R.raw.wrong_answer_sound)
    }
    
    fun playJokerUse() {
        // Joker sesi olarak şimdilik 'correct' sesi, ama belki ilerde değişir
         playSound(R.raw.correct_answer_sound)
    }
    
    fun playButtonClick() {
        // Buton tıklama sesi (sistem sesi kullanabiliriz veya sessiz)
    }

    fun release() {
        soundPool?.release()
        soundPool = null
        Log.d("SoundManager", "SoundPool serbest bırakıldı.")
    }
}