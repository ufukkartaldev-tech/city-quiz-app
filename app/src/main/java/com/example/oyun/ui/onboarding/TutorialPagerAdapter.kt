package com.example.oyun.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.databinding.ItemOnboardingSlideBinding

class TutorialPagerAdapter(private val pages: List<TutorialPage>) :
    RecyclerView.Adapter<TutorialPagerAdapter.TutorialViewHolder>() {

    inner class TutorialViewHolder(private val binding: ItemOnboardingSlideBinding) :
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(page: TutorialPage) {
            binding.slideTitle.text = page.title
            binding.slideDescription.text = page.description
            binding.slideImage.setImageResource(page.imageRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TutorialViewHolder {
        val binding = ItemOnboardingSlideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TutorialViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TutorialViewHolder, position: Int) {
        holder.bind(pages[position])
    }

    override fun getItemCount() = pages.size
}
