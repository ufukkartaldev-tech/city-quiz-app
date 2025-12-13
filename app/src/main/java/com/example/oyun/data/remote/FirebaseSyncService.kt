package com.example.oyun.data.remote

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.example.oyun.data.HighScore
import com.example.oyun.data.HighScoreDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseSyncService @Inject constructor(
    private val context: Context,
    private val highScoreDao: HighScoreDao,
    private val remoteRepository: HighScoreRemoteRepository
) {

    private val scope = CoroutineScope(Dispatchers.IO)

    fun syncUnsyncedScores() {
        if (!isNetworkAvailable()) {
            Log.d("FirebaseSync", "No network, skipping sync")
            return
        }

        scope.launch {
            try {
                val unsyncedScores = highScoreDao.getUnsyncedScores()
                Log.d("FirebaseSync", "Found ${unsyncedScores.size} unsynced scores")

                unsyncedScores.forEach { localScore ->
                    val result = remoteRepository.uploadHighScore(localScore)
                    if (result.isSuccess) {
                        // Mark as synced in local DB
                        highScoreDao.update(localScore.copy(isSynced = true))
                        Log.d("FirebaseSync", "Synced score: ${localScore.userName} - ${localScore.score}")
                    } else {
                        Log.e("FirebaseSync", "Failed to sync score: ${result.exceptionOrNull()?.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("FirebaseSync", "Sync error: ${e.message}")
            }
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }
}
