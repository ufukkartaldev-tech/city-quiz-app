package com.example.oyun.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface HighScoreDao {

    @Insert
    suspend fun insert(highScore: HighScore)

    @Query(
        """
        SELECT * FROM high_scores 
        ORDER BY score DESC, timestamp ASC 
        LIMIT :limit
        """
    )
    suspend fun getTopScores(limit: Int = 10): List<HighScore>

    @Query("SELECT * FROM high_scores WHERE isSynced = 0")
    suspend fun getUnsyncedScores(): List<HighScore>

    @Update
    suspend fun update(highScore: HighScore)
}


