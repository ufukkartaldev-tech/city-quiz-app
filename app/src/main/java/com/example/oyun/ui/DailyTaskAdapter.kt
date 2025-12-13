package com.example.oyun.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.databinding.ItemDailyTaskBinding
import com.example.oyun.data.DailyTask

class DailyTaskAdapter(
    private var tasks: List<DailyTask>
) : RecyclerView.Adapter<DailyTaskAdapter.TaskViewHolder>() {

    class TaskViewHolder(val binding: ItemDailyTaskBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemDailyTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        
        with(holder.binding) {
            taskTitle.text = task.title
            taskDescription.text = task.description
            taskReward.text = task.reward
            
            // İlerleme çubuğu ve metni
            taskProgressBar.max = task.maxProgress
            taskProgressBar.progress = task.progress
            taskProgress.text = "${task.progress}/${task.maxProgress}"
            
            // Tamamlandı durumu
            if (task.isCompleted) {
                taskStatusIcon.text = "✅"
                root.alpha = 0.7f
            } else {
                taskStatusIcon.text = "⏳" // veya boş
                root.alpha = 1.0f
            }
        }
    }

    override fun getItemCount() = tasks.size

    fun updateTasks(newTasks: List<DailyTask>) {
        tasks = newTasks
        notifyDataSetChanged()
    }
}
