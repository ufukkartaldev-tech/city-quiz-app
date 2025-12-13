package com.example.oyun.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "high_scores")
data class HighScore(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val userName: String,
    val score: Int,
    val correctAnswers: Int,
    val totalQuestions: Int,
    val level: Int,
    val timestamp: Long,
    val isSynced: Boolean = false
)


