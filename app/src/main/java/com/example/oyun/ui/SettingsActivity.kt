package com.example.oyun.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import com.example.oyun.R
import com.example.oyun.databinding.ActivitySettingsBinding
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        setupUI()
    }

    private fun setupUI() {
        // Toolbar navigation
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupDarkMode()
        setupLanguage()
        setupSoundSettings()
        setupAccountSettings()
        setupAppInfo()
    }

    private fun setupDarkMode() {
        // Dark mode durumunu yükle
        val isDarkMode = prefs.getBoolean("dark_mode", false)
        binding.darkModeSwitch.isChecked = isDarkMode

        binding.darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
            // Ayarı kaydet
            prefs.edit().putBoolean("dark_mode", isChecked).apply()
            
            // Temayı değiştir
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
            
            // Aktiviteyi yeniden oluştur
            recreate()
        }
    }

    private fun setupLanguage() {
        // Dil seçenekleri
        val languages = arrayOf("🇹🇷 Türkçe", "🇬🇧 English")
        val languageCodes = arrayOf("tr", "en")
        
        // Spinner adapter
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languages)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.languageSpinner.adapter = adapter
        
        // Mevcut dili yükle
        val currentLanguage = prefs.getString("app_language", "tr") ?: "tr"
        val currentIndex = languageCodes.indexOf(currentLanguage)
        if (currentIndex >= 0) {
            binding.languageSpinner.setSelection(currentIndex)
        }
        
        // Dil değişikliği
        binding.languageSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedLanguage = languageCodes[position]
                val savedLanguage = prefs.getString("app_language", "tr")
                
                if (selectedLanguage != savedLanguage) {
                    // Dili kaydet
                    prefs.edit().putString("app_language", selectedLanguage).apply()
                    
                    // Dili değiştir
                    setAppLocale(selectedLanguage)
                    
                    // Aktiviteyi yeniden başlat
                    showToast(if (selectedLanguage == "tr") getString(R.string.language_changed) else getString(R.string.language_changed_en))
                    recreate()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Hiçbir şey yapma
            }
        }
    }
    
    private fun setAppLocale(languageCode: String) {
        val localeList = LocaleListCompat.forLanguageTags(languageCode)
        AppCompatDelegate.setApplicationLocales(localeList)
    }

    private fun setupSoundSettings() {
        // Ses Efektleri
        binding.soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        binding.soundSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("sound_enabled", isChecked).apply()
            showToast(if (isChecked) getString(R.string.sound_on) else getString(R.string.sound_off))
        }

        // Müzik
        binding.musicSwitch.isChecked = prefs.getBoolean("music_enabled", true)
        binding.musicSwitch.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("music_enabled", isChecked).apply()
            showToast(if (isChecked) getString(R.string.music_on) else getString(R.string.music_off))
        }
    }

    private fun setupAccountSettings() {
        val currentUser = FirebaseAuth.getInstance().currentUser

        // Profil Bilgileri
        binding.profileButton.setOnClickListener {
            if (currentUser != null) {
                showProfileDialog()
            } else {
                showToast(getString(R.string.login_required))
            }
        }

        // Profil Değiştir
        binding.changeProfileButton.setOnClickListener {
            val intent = Intent(this, ProfileSelectionActivity::class.java)
            startActivity(intent)
        }

        // Çıkış Yap
        binding.signOutButton.setOnClickListener {
            showSignOutDialog()
        }
    }

    private fun setupAppInfo() {
        // Versiyon bilgisi
        binding.versionText.text = getString(R.string.version_format, "1.0.0")

        // Gizlilik Politikası
        binding.privacyPolicyButton.setOnClickListener {
            val intent = Intent(this, PrivacyPolicyActivity::class.java)
            startActivity(intent)
        }

        // Hakkında
        binding.aboutButton.setOnClickListener {
            showAboutDialog()
        }
    }

    private fun showProfileDialog() {
        val user = FirebaseAuth.getInstance().currentUser
        val message = getString(
            R.string.profile_info_format,
            user?.displayName ?: getString(R.string.guest),
            user?.email ?: "Yok",
            user?.uid ?: "Yok"
        )

        AlertDialog.Builder(this)
            .setTitle(getString(R.string.profile_info_title))
            .setMessage(message)
            .setPositiveButton(getString(R.string.ok), null)
            .show()
    }

    private fun showSignOutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.sign_out_title))
            .setMessage(getString(R.string.sign_out_message))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                // Firebase'den çıkış yap
                FirebaseAuth.getInstance().signOut()
                
                // SharedPreferences'ı temizle
                prefs.edit().clear().apply()
                
                // Auth ekranına dön
                val intent = Intent(this, AuthActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

    private fun showAboutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.about_title))
            .setMessage(getString(R.string.about_message))
            .setPositiveButton(getString(R.string.ok), null)
            .setNeutralButton(getString(R.string.feedback)) { _, _ ->
                sendFeedback()
            }
            .show()
    }

    private fun sendFeedback() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:destek@sehirbilgiyarismasi.com")
            putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject))
        }
        
        try {
            startActivity(Intent.createChooser(intent, getString(R.string.email_chooser_title)))
        } catch (e: Exception) {
            showToast(getString(R.string.email_not_found))
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
