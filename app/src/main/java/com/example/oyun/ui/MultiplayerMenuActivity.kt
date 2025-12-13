package com.example.oyun.ui

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oyun.R
import com.example.oyun.data.GameRoom
import com.example.oyun.databinding.ActivityMultiplayerMenuBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.AndroidEntryPoint
import kotlin.random.Random

@AndroidEntryPoint
class MultiplayerMenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMultiplayerMenuBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMultiplayerMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()
        setupRecyclerView()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupButtons() {
        binding.createRoomButton.setOnClickListener {
            showCreateRoomDialog()
        }

        binding.joinRoomButton.setOnClickListener {
            showJoinRoomDialog()
        }
    }

    private fun showCreateRoomDialog() {
        val options = arrayOf("Herkese Açık Oda", "Özel Oda (Arkadaşınla Oyna)")
        AlertDialog.Builder(this)
            .setTitle("Oda Tipi Seç")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> createRoom(isPrivate = false)
                    1 -> createRoom(isPrivate = true)
                }
            }
            .show()
    }

    private fun showJoinRoomDialog() {
        val options = arrayOf("Rastgele Katıl", "Kod ile Katıl")
        AlertDialog.Builder(this)
            .setTitle("Nasıl Katılmak İstersin?")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> joinRandomRoom()
                    1 -> showEnterCodeDialog()
                }
            }
            .show()
    }

    private fun showEnterCodeDialog() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        input.hint = "6 Haneli Kod"

        AlertDialog.Builder(this)
            .setTitle("Oda Kodunu Gir")
            .setView(input)
            .setPositiveButton("Katıl") { _, _ ->
                val code = input.text.toString()
                if (code.length == 6) {
                    joinRoomByCode(code)
                } else {
                    Toast.makeText(this, "Geçersiz kod", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun createRoom(isPrivate: Boolean) {
        val userId = auth.currentUser?.uid ?: return
        val userName = auth.currentUser?.displayName ?: "Oyuncu"
        
        val roomId = db.collection("rooms").document().id
        val roomCode = if (isPrivate) Random.nextInt(100000, 999999).toString() else null
        val seed = System.currentTimeMillis() // Her iki oyuncu da aynı seed'i kullanacak

        val room = GameRoom(
            roomId = roomId,
            hostId = userId,
            hostName = userName,
            status = "WAITING",
            isPrivate = isPrivate,
            roomCode = roomCode,
            questionSeed = seed
        )

        db.collection("rooms").document(roomId).set(room)
            .addOnSuccessListener {
                startGame(roomId, isHost = true)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Oda oluşturulamadı: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinRandomRoom() {
        val userId = auth.currentUser?.uid ?: return
        
        db.collection("rooms")
            .whereEqualTo("status", "WAITING")
            .whereEqualTo("isPrivate", false)
            .orderBy("roomId", Query.Direction.ASCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    AlertDialog.Builder(this)
                        .setTitle("Oda Bulunamadı")
                        .setMessage("Şu an bekleyen açık oda yok. Yeni bir oda oluşturmak ister misin?")
                        .setPositiveButton("Oluştur") { _, _ -> createRoom(isPrivate = false) }
                        .setNegativeButton("İptal", null)
                        .show()
                } else {
                    val room = snapshot.documents[0].toObject(GameRoom::class.java)
                    room?.let { joinRoomSafe(it.roomId) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinRoomByCode(code: String) {
        db.collection("rooms")
            .whereEqualTo("roomCode", code)
            .whereEqualTo("status", "WAITING")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    Toast.makeText(this, "Oda bulunamadı veya oyun başlamış", Toast.LENGTH_SHORT).show()
                } else {
                    val room = snapshot.documents[0].toObject(GameRoom::class.java)
                    room?.let { joinRoomSafe(it.roomId) }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Hata: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun joinRoomSafe(roomId: String) {
        val userId = auth.currentUser?.uid ?: return
        val userName = auth.currentUser?.displayName ?: "Misafir"
        val roomRef = db.collection("rooms").document(roomId)

        db.runTransaction { transaction ->
            val snapshot = transaction.get(roomRef)
            val room = snapshot.toObject(GameRoom::class.java)
            
            if (room == null) {
                throw Exception("Oda bulunamadı")
            }
            
            if (room.status != "WAITING") {
                throw Exception("Oda artık uygun değil")
            }
            
            if (room.hostId == userId) {
                 // Zaten host biziz (bu durum normal akışta olmamalı ama önlem)
                 return@runTransaction
            }

            if (room.guestId != null) {
                throw Exception("Oda dolu")
            }

            // Odaya katılabiliriz
            transaction.update(roomRef, "guestId", userId)
            transaction.update(roomRef, "guestName", userName)
            transaction.update(roomRef, "status", "PLAYING")
        }.addOnSuccessListener {
            startGame(roomId, isHost = false)
        }.addOnFailureListener { e ->
            val message = if (e.message == "Oda dolu" || e.message == "Oda artık uygun değil") 
                "Bu oda maalesef doldu." else "Odaya katılırken hata oluştu: ${e.message}"
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun startGame(roomId: String, isHost: Boolean) {
        val intent = Intent(this, MultiplayerGameActivity::class.java)
        intent.putExtra("ROOM_ID", roomId)
        intent.putExtra("IS_HOST", isHost)
        startActivity(intent)
    }

    private fun setupRecyclerView() {
        binding.roomsRecyclerView.layoutManager = LinearLayoutManager(this)
        // İleride buraya açık odaları listeleyen bir adapter eklenebilir
    }
}
