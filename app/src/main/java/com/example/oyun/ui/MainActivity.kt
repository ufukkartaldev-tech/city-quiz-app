package com.example.oyun.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.oyun.R
import com.example.oyun.data.QuestionManager
import com.example.oyun.data.remote.AuthRepository
import com.example.oyun.databinding.ActivityMainBinding
import com.example.oyun.managers.AdManager
import com.example.oyun.ui.main.ConsentManager
import com.example.oyun.ui.main.ProfileManager
import com.example.oyun.ui.main.RewardedAdHandler
import com.example.oyun.utils.setSafeOnClickListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var consentManager: ConsentManager
    private lateinit var profileManager: ProfileManager
    private lateinit var rewardedAdHandler: RewardedAdHandler

    @Inject
    lateinit var questionManager: QuestionManager
    @Inject
    lateinit var authRepository: AuthRepository
    @Inject
    lateinit var adManager: AdManager

    override fun onCreate(savedInstanceState: Bundle?) {
        // Dark mode ve dil ayarlarını yükle
        loadDarkModePreference()
        loadLanguagePreference()
        
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeManagers()
        requestUserConsent()
    }

    private fun loadDarkModePreference() {
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }

    private fun loadLanguagePreference() {
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val language = prefs.getString("app_language", "tr") ?: "tr"
        
        val localeList = LocaleListCompat.forLanguageTags(language)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun initializeManagers() {
        profileManager = ProfileManager(
            prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE),
            authRepository = authRepository,
            questionManager = questionManager
        )

        rewardedAdHandler = RewardedAdHandler(
            context = this,
            adManager = adManager,
            profileManager = profileManager,
            onRewardGranted = {
                // Joker sayıları güncellendi, gerekirse UI'ı yenile
            }
        )

        consentManager = ConsentManager(
            context = this,
            onConsentComplete = ::setupUI
        )
    }

    private fun requestUserConsent() {
        consentManager.requestConsent()
    }

    override fun onResume() {
        super.onResume()
        updateWelcomeMessage()
        setupContinueButton()  // Devam Et butonu durumunu güncelle
    }

    private fun setupUI() {
        updateWelcomeMessage()
        setupNavigationButtons()
        Log.d(TAG, "Uygulama başlatıldı.")
    }

    private fun updateWelcomeMessage() {
        val userName = profileManager.getActiveUserDisplayName()
        binding.welcomeText.text = getString(R.string.welcome_user, userName)
    }

    private fun setupNavigationButtons() {
        // Yeni Oyun butonu
        binding.startButton.setSafeOnClickListener {
            navigateToGame(level = 1, isNewGame = true)
        }

        // Devam Et butonu - Kayıtlı oyun kontrolü
        setupContinueButton()

        // Kart tıklamaları
        binding.jokerModeCard.setSafeOnClickListener {
            startActivity(Intent(this, JokerModeActivity::class.java))
        }

        binding.levelSelectCard.setSafeOnClickListener {
            startActivity(Intent(this, LevelSelectionActivity::class.java))
        }

        binding.highScoresCard.setSafeOnClickListener {
            startActivity(Intent(this, HighScoreActivity::class.java))
        }

        binding.settingsButtonAnchor.setSafeOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        binding.achievementsCard.setSafeOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        binding.dailyTasksCard.setSafeOnClickListener {
            startActivity(Intent(this, DailyTasksActivity::class.java))
        }

        binding.multiplayerButton.setSafeOnClickListener {
            startActivity(Intent(this, MultiplayerMenuActivity::class.java))
        }

        binding.watchAdCard.setSafeOnClickListener {
            rewardedAdHandler.showRewardedAdDialog()
        }

        binding.friendsCard.setSafeOnClickListener {
            startActivity(Intent(this, com.example.oyun.ui.friends.FriendsActivity::class.java))
        }
    }
    
    /**
     * Devam Et butonunu kayıtlı oyun durumuna göre ayarlar
     */
    private fun setupContinueButton() {
        val currentLevel = profileManager.getCurrentLevel()
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        val score = prefs.getInt("profile_${profileManager.getActiveUserDisplayName()}_score", 0)
        val lives = prefs.getInt("profile_${profileManager.getActiveUserDisplayName()}_lives", 3)
        
        // Kayıtlı oyun var mı kontrol et
        val hasSavedGame = currentLevel > 1 || score > 0 || lives != 3
        
        if (hasSavedGame) {
            // Kayıtlı oyun var - Butonu aktif et
            binding.continueButton.isEnabled = true
            binding.continueButton.alpha = 1.0f
            binding.continueButton.setSafeOnClickListener {
                navigateToGame(level = currentLevel, isNewGame = false)
            }
        } else {
            // Kayıtlı oyun yok - Butonu pasif et
            binding.continueButton.isEnabled = false
            binding.continueButton.alpha = 0.5f
            binding.continueButton.setOnClickListener {
                android.widget.Toast.makeText(
                    this,
                    "Kayıtlı oyun bulunamadı. Önce yeni oyun başlatın.",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun navigateToGame(level: Int, isNewGame: Boolean) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("LEVEL", level)
            putExtra("IS_NEW_GAME", isNewGame)
        }
        startActivity(intent)
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}