package com.example.oyun.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.oyun.R
import com.example.oyun.ui.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class QuizFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationManager: NotificationManager

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Bildirim tipine göre işle
        val type = message.data["type"] ?: return
        
        when (type) {
            "friend_request" -> handleFriendRequest(message.data)
            "friend_accepted" -> handleFriendAccepted(message.data)
            "game_invite" -> handleGameInvite(message.data)
            "daily_task" -> handleDailyTask(message.data)
            "achievement" -> handleAchievement(message.data)
            "leaderboard" -> handleLeaderboard(message.data)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Token'ı Firestore'a kaydet
        saveTokenToFirestore(token)
    }

    private fun handleFriendRequest(data: Map<String, String>) {
        val username = data["username"] ?: "Birileri"
        
        showNotification(
            channelId = CHANNEL_FRIENDS,
            title = "Yeni Arkadaşlık İsteği",
            message = "$username seni arkadaş olarak eklemek istiyor",
            notificationId = NOTIFICATION_FRIEND_REQUEST
        )
    }

    private fun handleFriendAccepted(data: Map<String, String>) {
        val username = data["username"] ?: "Birileri"
        
        showNotification(
            channelId = CHANNEL_FRIENDS,
            title = "Arkadaşlık Kabul Edildi",
            message = "$username arkadaşlık isteğini kabul etti",
            notificationId = NOTIFICATION_FRIEND_ACCEPTED
        )
    }

    private fun handleGameInvite(data: Map<String, String>) {
        val username = data["username"] ?: "Birileri"
        val roomId = data["roomId"] ?: ""
        
        showNotification(
            channelId = CHANNEL_GAME,
            title = "Oyun Daveti",
            message = "$username seni oyuna davet ediyor",
            notificationId = NOTIFICATION_GAME_INVITE,
            extras = mapOf("roomId" to roomId)
        )
    }

    private fun handleDailyTask(data: Map<String, String>) {
        showNotification(
            channelId = CHANNEL_TASKS,
            title = "Günlük Görevler Yenilendi",
            message = "Yeni görevleri tamamla, ödülleri kazan!",
            notificationId = NOTIFICATION_DAILY_TASK
        )
    }

    private fun handleAchievement(data: Map<String, String>) {
        val achievementName = data["name"] ?: "Yeni Başarım"
        
        showNotification(
            channelId = CHANNEL_ACHIEVEMENTS,
            title = "Başarım Kazandın! 🏆",
            message = achievementName,
            notificationId = NOTIFICATION_ACHIEVEMENT
        )
    }

    private fun handleLeaderboard(data: Map<String, String>) {
        val message = data["message"] ?: "Liderlik tablosunda değişiklik var"
        
        showNotification(
            channelId = CHANNEL_LEADERBOARD,
            title = "Liderlik Tablosu",
            message = message,
            notificationId = NOTIFICATION_LEADERBOARD
        )
    }

    private fun showNotification(
        channelId: String,
        title: String,
        message: String,
        notificationId: Int,
        extras: Map<String, String> = emptyMap()
    ) {
        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            extras.forEach { (key, value) ->
                putExtra(key, value)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            notificationId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val (name, description, importance) = when (channelId) {
                CHANNEL_FRIENDS -> Triple(
                    "Arkadaşlar",
                    "Arkadaşlık istekleri ve bildirimleri",
                    NotificationManager.IMPORTANCE_HIGH
                )
                CHANNEL_GAME -> Triple(
                    "Oyun Davetleri",
                    "Multiplayer oyun davetleri",
                    NotificationManager.IMPORTANCE_HIGH
                )
                CHANNEL_TASKS -> Triple(
                    "Günlük Görevler",
                    "Günlük görev hatırlatıcıları",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                CHANNEL_ACHIEVEMENTS -> Triple(
                    "Başarımlar",
                    "Kazanılan başarımlar",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                CHANNEL_LEADERBOARD -> Triple(
                    "Liderlik Tablosu",
                    "Liderlik tablosu güncellemeleri",
                    NotificationManager.IMPORTANCE_LOW
                )
                else -> return
            }

            val channel = NotificationChannel(channelId, name, importance).apply {
                this.description = description
            }

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun saveTokenToFirestore(token: String) {
        // TODO: Token'ı Firestore'a kaydet
        // Bu, FriendsRepository veya AuthRepository üzerinden yapılabilir
    }

    companion object {
        private const val CHANNEL_FRIENDS = "friends_channel"
        private const val CHANNEL_GAME = "game_channel"
        private const val CHANNEL_TASKS = "tasks_channel"
        private const val CHANNEL_ACHIEVEMENTS = "achievements_channel"
        private const val CHANNEL_LEADERBOARD = "leaderboard_channel"

        private const val NOTIFICATION_FRIEND_REQUEST = 1001
        private const val NOTIFICATION_FRIEND_ACCEPTED = 1002
        private const val NOTIFICATION_GAME_INVITE = 2001
        private const val NOTIFICATION_DAILY_TASK = 3001
        private const val NOTIFICATION_ACHIEVEMENT = 4001
        private const val NOTIFICATION_LEADERBOARD = 5001
    }
}
