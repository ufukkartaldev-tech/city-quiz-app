package com.example.oyun.ui.multiplayer

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.example.oyun.R
import com.example.oyun.data.GameRoom

/**
 * Multiplayer oyun sonu dialogunu yöneten sınıf
 */
class GameOverDialogManager(private val context: Context) {

    fun showGameOverDialog(
        room: GameRoom,
        onReturnToMenu: () -> Unit
    ) {
        val winnerText = determineWinner(room)
        val message = formatGameOverMessage(room, winnerText)

        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.game_over_title))
            .setMessage(message)
            .setPositiveButton(context.getString(R.string.return_menu)) { _, _ ->
                onReturnToMenu()
            }
            .setCancelable(false)
            .show()
    }

    private fun determineWinner(room: GameRoom): String {
        return when {
            room.hostScore > room.guestScore -> {
                context.getString(R.string.winner_format, room.hostName)
            }
            room.guestScore > room.hostScore -> {
                context.getString(R.string.winner_format, room.guestName)
            }
            else -> {
                context.getString(R.string.draw)
            }
        }
    }

    private fun formatGameOverMessage(room: GameRoom, winnerText: String): String {
        return context.getString(
            R.string.game_over_message_format,
            winnerText,
            room.hostName, room.hostScore,
            room.guestName, room.guestScore
        )
    }
}
