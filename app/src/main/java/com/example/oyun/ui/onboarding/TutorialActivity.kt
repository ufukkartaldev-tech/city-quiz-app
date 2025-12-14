package com.example.oyun.ui.onboarding

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.oyun.R
import com.example.oyun.databinding.ActivityTutorialBinding
import com.example.oyun.ui.MainActivity
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

data class TutorialPage(
    val title: String,
    val description: String,
    val imageRes: Int,
    val isInteractive: Boolean = false
)

@AndroidEntryPoint
class TutorialActivity : AppCompatActivity() {

    @Inject
    lateinit var prefs: SharedPreferences
    
    private lateinit var binding: ActivityTutorialBinding
    private lateinit var adapter: TutorialPagerAdapter

    private val tutorialPages = listOf(
        TutorialPage(
            title = "Hoş Geldin! 🎮",
            description = "Türkiye'nin en eğlenceli şehir bilgi yarışmasına hoş geldin! Hazır mısın?",
            imageRes = R.drawable.onboarding_welcome
        ),
        TutorialPage(
            title = "Sorulara Cevap Ver 📝",
            description = "Her seviyede 10 soru var. Doğru cevap ver, puan kazan!",
            imageRes = R.drawable.onboarding_questions
        ),
        TutorialPage(
            title = "Jokerlerini Kullan 🃏",
            description = "50-50: İki yanlış şıkkı kaldır\nAtla: Soruyu atla\nCan Kazan: Ekstra can kazan",
            imageRes = R.drawable.onboarding_jokers,
            isInteractive = true
        ),
        TutorialPage(
            title = "Puan Kazan, Seviye Atla 🏆",
            description = "Her doğru cevap 10 puan! 10 soruyu tamamla, yeni seviyeye geç!",
            imageRes = R.drawable.onboarding_score
        ),
        TutorialPage(
            title = "Arkadaşlarınla Yarış 👥",
            description = "Multiplayer modda arkadaşlarınla gerçek zamanlı yarış!",
            imageRes = R.drawable.onboarding_multiplayer
        ),
        TutorialPage(
            title = "Başarımları Topla 🎖️",
            description = "Özel görevleri tamamla, rozetleri kazan!",
            imageRes = R.drawable.onboarding_achievements
        ),
        TutorialPage(
            title = "Hadi Başlayalım! 🚀",
            description = "Her şey hazır! İlk oyununu oynamaya hazır mısın?",
            imageRes = R.drawable.onboarding_start
        )
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTutorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupViewPager()
        setupButtons()
    }

    private fun setupViewPager() {
        adapter = TutorialPagerAdapter(tutorialPages)
        binding.viewPager.adapter = adapter

        // Tab indicator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()

        // Sayfa değişikliğini dinle
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateButtons(position)
            }
        })
    }

    private fun setupButtons() {
        binding.btnSkip.setOnClickListener {
            finishTutorial()
        }

        binding.btnNext.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem < tutorialPages.size - 1) {
                binding.viewPager.currentItem = currentItem + 1
            } else {
                finishTutorial()
            }
        }

        binding.btnBack.setOnClickListener {
            val currentItem = binding.viewPager.currentItem
            if (currentItem > 0) {
                binding.viewPager.currentItem = currentItem - 1
            }
        }
    }

    private fun updateButtons(position: Int) {
        // İlk sayfada "Geri" butonu gizli
        binding.btnBack.visibility = if (position == 0) {
            android.view.View.INVISIBLE
        } else {
            android.view.View.VISIBLE
        }

        // Son sayfada "İleri" butonu "Başla" olsun
        if (position == tutorialPages.size - 1) {
            binding.btnNext.text = "Başla"
            binding.btnSkip.visibility = android.view.View.INVISIBLE
        } else {
            binding.btnNext.text = "İleri"
            binding.btnSkip.visibility = android.view.View.VISIBLE
        }
    }

    private fun finishTutorial() {
        // Tutorial'ı tamamlandı olarak işaretle
        prefs.edit().putBoolean("tutorial_completed", true).apply()

        // Ana ekrana geç
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
