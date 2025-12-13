package com.example.oyun.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.databinding.ItemAchievementBinding
import com.example.oyun.data.Achievement

class AchievementAdapter(
    private var achievements: List<Achievement>
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    class AchievementViewHolder(val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        val achievement = achievements[position]
        
        with(holder.binding) {
            achievementIcon.text = achievement.iconEmoji
            achievementTitle.text = achievement.title
            achievementDescription.text = achievement.description
            
            // Kilidi açılmışsa farklı görünüm
            if (achievement.isUnlocked) {
                root.alpha = 1.0f
                achievementIcon.alpha = 1.0f
            } else {
                root.alpha = 0.5f
                achievementIcon.alpha = 0.3f
            }
        }
    }

    override fun getItemCount() = achievements.size

    fun updateAchievements(newAchievements: List<Achievement>) {
        achievements = newAchievements
        notifyDataSetChanged()
    }
}
