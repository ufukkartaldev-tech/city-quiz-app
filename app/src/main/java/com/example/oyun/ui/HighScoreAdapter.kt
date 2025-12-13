package com.example.oyun.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.data.HighScore
import com.example.oyun.databinding.ItemHighScoreBinding

class HighScoreAdapter(
    private var scores: List<HighScore>
) : RecyclerView.Adapter<HighScoreAdapter.HighScoreViewHolder>() {

    fun updateScores(newScores: List<HighScore>) {
        scores = newScores
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HighScoreViewHolder {
        val binding = ItemHighScoreBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HighScoreViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HighScoreViewHolder, position: Int) {
        holder.bind(scores[position], position + 1)
    }

    override fun getItemCount() = scores.size

    class HighScoreViewHolder(
        private val binding: ItemHighScoreBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(score: HighScore, rank: Int) {
            val medal = when (rank) {
                1 -> "🥇"
                2 -> "🥈"
                3 -> "🥉"
                else -> "$rank."
            }

            binding.rankText.text = medal
            binding.nameText.text = score.userName
            binding.scoreText.text = score.score.toString()
        }
    }
}
