package com.example.oyun.ui.category

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.R
import com.example.oyun.databinding.ItemCategoryBinding

/**
 * Kategori RecyclerView Adapter'ı
 */
class CategoryAdapter(
    private val onCategoryClick: (QuestionCategory) -> Unit
) : ListAdapter<QuestionCategory, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val binding = ItemCategoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CategoryViewHolder(binding, onCategoryClick)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CategoryViewHolder(
        private val binding: ItemCategoryBinding,
        private val onCategoryClick: (QuestionCategory) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(category: QuestionCategory) {
            binding.apply {
                // Kategori adı
                tvCategoryName.text = category.name
                
                // İkon (emoji)
                tvCategoryIcon.text = category.icon
                
                // Açıklama
                tvCategoryDescription.text = category.description
                
                // Soru sayısı
                tvQuestionCount.text = "${category.questionCount} soru"
                
                // Renk
                cardCategory.setCardBackgroundColor(category.color)
                
                // Kilitli mi?
                if (category.isLocked) {
                    // Kilitli görünüm
                    tvCategoryName.alpha = 0.5f
                    tvCategoryDescription.alpha = 0.5f
                    tvQuestionCount.text = "🔒 Level ${category.requiredLevel}"
                    cardCategory.alpha = 0.6f
                    cardCategory.isClickable = false
                } else {
                    // Açık görünüm
                    tvCategoryName.alpha = 1f
                    tvCategoryDescription.alpha = 1f
                    cardCategory.alpha = 1f
                    cardCategory.isClickable = true
                    
                    // Tıklama
                    cardCategory.setOnClickListener {
                        onCategoryClick(category)
                    }
                }
                
                // Animasyon
                cardCategory.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(if (category.isLocked) 0.6f else 1f)
                    .setDuration(200)
                    .start()
            }
        }
    }

    private class CategoryDiffCallback : DiffUtil.ItemCallback<QuestionCategory>() {
        override fun areItemsTheSame(oldItem: QuestionCategory, newItem: QuestionCategory): Boolean {
            return oldItem.code == newItem.code
        }

        override fun areContentsTheSame(oldItem: QuestionCategory, newItem: QuestionCategory): Boolean {
            return oldItem == newItem
        }
    }
}
