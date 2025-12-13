package com.example.oyun.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oyun.databinding.ActivityDailyTasksBinding // KROKİYİ BURADA TANIYORUZ
import com.example.oyun.data.DailyTaskData
import com.example.oyun.data.DailyTask
import com.example.oyun.managers.DailyTaskManager

class DailyTasksActivity : AppCompatActivity() {

    // KROKİ (BINDING) İÇİN MASADA BİR YER AÇIYORUZ
    private lateinit var binding: ActivityDailyTasksBinding

    private lateinit var taskAdapter: DailyTaskAdapter
    private lateinit var dailyTaskManager: DailyTaskManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // DÜKKÂNI ESKİ USUL DEĞİL, YENİ KROKİYLE AÇIYORUZ
        binding = ActivityDailyTasksBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefs = getSharedPreferences("app_settings", MODE_PRIVATE)
        dailyTaskManager = DailyTaskManager(this, prefs)

        // ARTIK findViewById'a GEREK YOK!
        setupRecyclerView()
        loadTasks()

        // Toolbar navigation icon
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        // Ekrana geri dönüldüğünde görevleri yenilemek her zaman iyidir.
        loadTasks()
    }

    private fun setupRecyclerView() {
        taskAdapter = DailyTaskAdapter(emptyList())
        // RecyclerView'ı krokiden çağırıyoruz
        binding.tasksRecyclerView.adapter = taskAdapter
        binding.tasksRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun loadTasks() {
        val tasks = dailyTaskManager.getDailyTasks()
        taskAdapter.updateTasks(tasks)
    }
}
