package com.example.oyun.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.oyun.R
import com.example.oyun.databinding.ActivityLevelSelectionBinding
import com.google.android.material.card.MaterialCardView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LevelSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLevelSelectionBinding

    @Inject
    lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLevelSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupLevelCards()
    }

    private fun setupLevelCards() {
        val activeUser = prefs.getString("last_active_user", "Misafir") ?: "Misafir"
        val currentLevel = prefs.getInt("profile_${activeUser}_current_level", 1)  // ✅ Doğru anahtar

        // Layout'taki mevcut 4 card'ı ayarla
        setupCard(binding.level1Card, 1, currentLevel >= 1)
        setupCard(binding.level2Card, 2, currentLevel >= 2)
        setupCard(binding.level3Card, 3, currentLevel >= 3)
        setupCard(binding.level4Card, 4, currentLevel >= 4)
        
        // TODO: Kalan 6 level için layout'a card eklenecek veya
        // programatik olarak oluşturulacak
    }

    private fun setupCard(card: MaterialCardView, level: Int, isUnlocked: Boolean) {
        card.setOnClickListener {
            if (isUnlocked) {
                startLevel(level)
            }
        }

        // Kilit durumunu göster
        if (!isUnlocked) {
            card.alpha = 0.5f
            card.isClickable = false
        } else {
            card.alpha = 1.0f
            card.isClickable = true
        }
    }

    private fun startLevel(level: Int) {
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("LEVEL", level)
        intent.putExtra("IS_NEW_GAME", true)  // ✅ Seviye seçiminde yeni oyun
        startActivity(intent)
    }
}
