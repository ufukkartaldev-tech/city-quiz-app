package com.example.oyun.ui.category

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.oyun.R
import com.example.oyun.databinding.ActivityCategorySelectionBinding
import com.example.oyun.ui.GameActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Kategori Seçim Ekranı
 * Kullanıcı oyun kategorisi seçer (Coğrafya, Tarih, Kültür, vb.)
 */
@AndroidEntryPoint
class CategorySelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategorySelectionBinding
    private val viewModel: CategoryViewModel by viewModels()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorySelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupRecyclerView()
        observeViewModel()
        
        // Kategorileri yükle
        viewModel.loadCategories()
    }

    private fun setupUI() {
        // Geri butonu
        binding.btnBack.setOnClickListener {
            finish()
        }

        // Tüm kategoriler butonu
        binding.btnAllCategories.setOnClickListener {
            startGameWithCategory(null) // Tüm kategoriler
        }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter { category ->
            // Kategori seçildiğinde
            startGameWithCategory(category.code)
        }

        binding.rvCategories.apply {
            layoutManager = GridLayoutManager(this@CategorySelectionActivity, 2)
            adapter = this@CategorySelectionActivity.adapter
            setHasFixedSize(true)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                adapter.submitList(categories)
                
                // Loading gizle
                binding.progressBar.visibility = View.GONE
                binding.rvCategories.visibility = View.VISIBLE
            }
        }

        lifecycleScope.launch {
            viewModel.categoryStats.collect { stats ->
                // İstatistikleri göster (opsiyonel)
                updateStats(stats)
            }
        }
    }

    private fun updateStats(stats: Map<String, Int>) {
        // Her kategorideki soru sayısını göster
        // Adapter'a iletilecek
    }

    private fun startGameWithCategory(categoryCode: String?) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("CATEGORY", categoryCode)
            putExtra("START_LEVEL", 1)
        }
        startActivity(intent)
        finish()
    }
}
