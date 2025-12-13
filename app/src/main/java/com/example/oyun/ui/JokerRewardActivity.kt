package com.example.oyun.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.oyun.databinding.ActivityJokerRewardBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class JokerRewardActivity : AppCompatActivity() {

    private lateinit var binding: ActivityJokerRewardBinding

    @Inject
    lateinit var prefs: SharedPreferences
    
    @Inject
    lateinit var soundManager: com.example.oyun.managers.SoundManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJokerRewardBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Ses efekti
        soundManager.playLevelUp()

        // Rastgele bir joker ver
        giveRandomJoker()

        binding.continueButton.setOnClickListener {
            finish()
        }
    }

    private fun giveRandomJoker() {
        val activeUser = prefs.getString("last_active_user", "Misafir") ?: "Misafir"
        val jokerTypes = listOf("fiftyfifty", "skip", "gainlife")
        val randomJoker = jokerTypes.random()

        val key = "profile_${activeUser}_joker_${randomJoker}_count"
        val currentCount = prefs.getInt(key, 0)
        prefs.edit().putInt(key, currentCount + 1).apply()

        // Show which joker was earned
        val jokerName = when (randomJoker) {
            "fiftyfifty" -> "50-50"
            "skip" -> "Atla"
            "gainlife" -> "Can Kazan"
            else -> "Joker"
        }

        binding.jokerIcon.text = when (randomJoker) {
            "fiftyfifty" -> "5️⃣0️⃣"
            "skip" -> "⏭️"
            "gainlife" -> "❤️"
            else -> "🃏"
        }
        
        // Animasyon
        binding.jokerIcon.startAnimation(android.view.animation.AnimationUtils.loadAnimation(this, com.example.oyun.R.anim.fade_in_modern))
    }
}
