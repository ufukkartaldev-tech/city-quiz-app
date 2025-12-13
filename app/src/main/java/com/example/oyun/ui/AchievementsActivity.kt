package com.example.oyun.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oyun.databinding.ActivityAchievementsBinding
import com.example.oyun.data.AchievementData
import com.example.oyun.data.Achievement

class AchievementsActivity : AppCompatActivity() {

    // KROKİ (BINDING) İÇİN MASADA BİR YER AÇIYORUZ
    private lateinit var binding: ActivityAchievementsBinding

    private lateinit var achievementAdapter: AchievementAdapter
    private var activeUser = "Misafir"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DÜKKÂNI ESKİ USUL DEĞİL, YENİ KROKİYLE AÇIYORUZ
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)
        activeUser = prefs.getString("last_active_user", "Misafir") ?: "Misafir"

        // ARTIK findViewById'a GEREK YOK!
        setupRecyclerView()
        loadAchievements()

        // Toolbar navigation icon
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        achievementAdapter = AchievementAdapter(emptyList())
        // RecyclerView'ı krokiden çağırıyoruz
        binding.achievementsRecyclerView.adapter = achievementAdapter
        binding.achievementsRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadAchievements() {
        val prefs = getSharedPreferences("quiz_prefs", MODE_PRIVATE)

        // Tüm başarımları al ve kilit durumlarını kontrol et
        val achievementsWithStatus = AchievementData.achievements.map { achievement ->
            val isUnlocked = prefs.getBoolean("profile_${activeUser}_achievement_${achievement.id}", false)
            achievement.copy(isUnlocked = isUnlocked)
        }

        achievementAdapter.updateAchievements(achievementsWithStatus)
    }
}
