package com.example.oyun.ui

import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oyun.data.HighScoreDao
import com.example.oyun.data.remote.AuthRepository
import com.example.oyun.databinding.ActivityHighScoreBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class HighScoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHighScoreBinding

    @Inject
    lateinit var prefs: SharedPreferences

    @Inject
    lateinit var highScoreDao: HighScoreDao

    @Inject
    lateinit var authRepository: AuthRepository

    private lateinit var scoreAdapter: HighScoreAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHighScoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }

        setupRecyclerView()
        loadScores()
    }

    private fun setupRecyclerView() {
        scoreAdapter = HighScoreAdapter(emptyList())
        binding.scoresRecyclerView.adapter = scoreAdapter
        binding.scoresRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadScores() {
        lifecycleScope.launch {
            val topScores = highScoreDao.getTopScores(20)
            scoreAdapter.updateScores(topScores)
        }
    }
}
