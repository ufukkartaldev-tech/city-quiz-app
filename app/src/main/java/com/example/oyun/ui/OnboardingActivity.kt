package com.example.oyun.ui

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import com.example.oyun.databinding.ActivityOnboardingBinding
import com.google.android.material.tabs.TabLayoutMediator

/**
 * İlk açılışta gösterilen onboarding ekranı
 */
class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityOnboardingBinding
    
    private val slides = listOf(
        OnboardingSlide(
            title = "Hoş Geldin! 👋",
            description = "Bilgi yarışmasına hazır mısın? Heyecan verici sorular seni bekliyor!",
            imageRes = com.example.oyun.R.drawable.onboarding_welcome
        ),
        OnboardingSlide(
            title = "Sorulara Cevap Ver 📝",
            description = "10 seviye, her seviyede 10 soru! Her doğru cevap için puan kazan.",
            imageRes = com.example.oyun.R.drawable.onboarding_questions
        ),
        OnboardingSlide(
            title = "Joker Kullan 🃏",
            description = "50-50, Geç ve Can Kazan jokerlerini kullanarak zorlu soruları aş!",
            imageRes = com.example.oyun.R.drawable.onboarding_jokers
        ),
        OnboardingSlide(
            title = "Skor Kazan 🏆",
            description = "Yüksek skorları yakala, liderlik tablosuna çık ve başarımları topla!",
            imageRes = com.example.oyun.R.drawable.onboarding_score
        )
    )
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOnboardingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setupViewPager()
        setupButtons()
    }
    
    private fun setupViewPager() {
        val adapter = OnboardingAdapter(slides)
        binding.viewPager.adapter = adapter
        
        // Tab indicator
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { _, _ -> }.attach()
        
        // Sayfa değişikliğini dinle
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateButtons(position)
            }
        })
    }
    
    private fun setupButtons() {
        binding.skipButton.setOnClickListener {
            finishOnboarding()
        }
        
        binding.nextButton.setOnClickListener {
            if (binding.viewPager.currentItem < slides.size - 1) {
                binding.viewPager.currentItem += 1
            } else {
                finishOnboarding()
            }
        }
        
        binding.backButton.setOnClickListener {
            if (binding.viewPager.currentItem > 0) {
                binding.viewPager.currentItem -= 1
            }
        }
    }
    
    private fun updateButtons(position: Int) {
        // İlk sayfada geri butonu gizle
        binding.backButton.visibility = if (position == 0) {
            android.view.View.GONE
        } else {
            android.view.View.VISIBLE
        }
        
        // Son sayfada "Başla" yaz
        binding.nextButton.text = if (position == slides.size - 1) {
            "Başla"
        } else {
            "İleri"
        }
    }
    
    private fun finishOnboarding() {
        // Onboarding'i tamamlandı olarak işaretle
        getSharedPreferences("quiz_prefs", MODE_PRIVATE)
            .edit()
            .putBoolean("onboarding_completed", true)
            .apply()
        
        // Ana ekrana git
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}

/**
 * Onboarding slide veri sınıfı
 */
data class OnboardingSlide(
    val title: String,
    val description: String,
    val imageRes: Int
)
