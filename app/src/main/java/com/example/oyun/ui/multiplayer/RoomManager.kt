package com.example.oyun.ui.multiplayer

import android.widget.Toast
import com.example.oyun.R
import com.example.oyun.data.GameRoom
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

/**
 * Firebase Firestore ile oda yönetimini sağlayan sınıf
 */
class RoomManager(
    private val db: FirebaseFirestore,
    private val roomId: String,
    private val userId: String,
    private val isHost: Boolean,
    private val onRoomUpdate: (GameRoom) -> Unit,
    private val onError: (String) -> Unit
) {

    private var roomListener: ListenerRegistration? = null

    fun startListening() {
        val roomRef = db.collection("rooms").document(roomId)
        
        roomListener = roomRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                onError("Bağlantı kesildi!")
                return@addSnapshotListener
            }

            if (snapshot == null || !snapshot.exists()) {
                onError("Rakip oyundan ayrıldı.")
                return@addSnapshotListener
            }

            val room = snapshot.toObject(GameRoom::class.java)
            room?.let { onRoomUpdate(it) }
        }
    }

    fun stopListening() {
        roomListener?.remove()
        roomListener = null
    }

    fun updateScore(scoreToAdd: Int) {
        if (scoreToAdd <= 0) return

        val field = if (isHost) "hostScore" else "guestScore"
        val updates = hashMapOf<String, Any>(
            field to com.google.firebase.firestore.FieldValue.increment(scoreToAdd.toLong())
        )
        
        db.collection("rooms").document(roomId).update(updates)
    }

    fun sendEmoji(emoji: String) {
        val update = hashMapOf<String, Any>(
            "lastEmoji" to "${userId}|${emoji}|${System.currentTimeMillis()}"
        )
        db.collection("rooms").document(roomId).update(update)
    }

    fun markAsFinished() {
        val field = if (isHost) "hostFinished" else "guestFinished"
        db.collection("rooms").document(roomId).update(field, true)
    }
}
