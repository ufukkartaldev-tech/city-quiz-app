package com.example.oyun.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.databinding.ItemOnboardingSlideBinding

/**
 * Onboarding ViewPager adapter
 */
class OnboardingAdapter(
    private val slides: List<OnboardingSlide>
) : RecyclerView.Adapter<OnboardingAdapter.SlideViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SlideViewHolder {
        val binding = ItemOnboardingSlideBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SlideViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: SlideViewHolder, position: Int) {
        holder.bind(slides[position])
    }
    
    override fun getItemCount() = slides.size
    
    class SlideViewHolder(
        private val binding: ItemOnboardingSlideBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(slide: OnboardingSlide) {
            binding.slideTitle.text = slide.title
            binding.slideDescription.text = slide.description
            binding.slideImage.setImageResource(slide.imageRes)
        }
    }
}
