package com.example.oyun.managers

import android.util.Log
import com.example.oyun.data.HighScore
import com.example.oyun.data.HighScoreDao
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighScoreSyncManager @Inject constructor(
    private val highScoreDao: HighScoreDao
) {

    suspend fun syncPendingScores() = withContext(Dispatchers.IO) {
        val pending: List<HighScore> = try {
            highScoreDao.getUnsyncedScores()
        } catch (e: Exception) {
            Log.e("HighScoreSync", "DB error while fetching pending scores: ${e.message}")
            return@withContext
        }

        if (pending.isEmpty()) return@withContext

        val db = Firebase.firestore

        for (score in pending) {
            try {
                val data = mapOf(
                    "userName" to score.userName,
                    "score" to score.score,
                    "correctAnswers" to score.correctAnswers,
                    "totalQuestions" to score.totalQuestions,
                    "level" to score.level,
                    "timestamp" to score.timestamp
                )

                db.collection("leaderboard")
                    .add(data)
                    .await()

                highScoreDao.update(score.copy(isSynced = true))
            } catch (e: Exception) {
                Log.e("HighScoreSync", "Failed to sync score for ${score.userName}: ${e.message}")
                // Hata olursa isSynced=false kalır, sonraki sync denemesinde tekrar gönderilir.
            }
        }
    }
}


