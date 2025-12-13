package com.example.oyun.data.remote

import android.util.Log
import com.example.oyun.data.HighScore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HighScoreRemoteRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {

    companion object {
        private const val COLLECTION_HIGHSCORES = "highscores"
    }

    suspend fun uploadHighScore(highScore: HighScore): Result<Unit> {
        return try {
            val data = hashMapOf(
                "userName" to highScore.userName,
                "score" to highScore.score,
                "correctAnswers" to highScore.correctAnswers,
                "totalQuestions" to highScore.totalQuestions,
                "level" to highScore.level,
                "timestamp" to highScore.timestamp
            )

            firestore.collection(COLLECTION_HIGHSCORES)
                .document(highScore.userName + "_" + highScore.timestamp)
                .set(data)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("HighScoreRemoteRepo", "Upload failed: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun getTopHighScores(limit: Int = 50): Result<List<HighScore>> {
        return try {
            val querySnapshot = firestore.collection(COLLECTION_HIGHSCORES)
                .orderBy("score", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val highScores = querySnapshot.documents.mapNotNull { doc ->
                try {
                    HighScore(
                        userName = doc.getString("userName") ?: "Unknown",
                        score = (doc.getLong("score") ?: 0).toInt(),
                        correctAnswers = (doc.getLong("correctAnswers") ?: 0).toInt(),
                        totalQuestions = (doc.getLong("totalQuestions") ?: 0).toInt(),
                        level = (doc.getLong("level") ?: 1).toInt(),
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        isSynced = true
                    )
                } catch (e: Exception) {
                    null
                }
            }

            Result.success(highScores)
        } catch (e: Exception) {
            Log.e("HighScoreRemoteRepo", "Fetch failed: ${e.message}")
            Result.failure(e)
        }
    }
}
