package com.example.oyun.ui.friends

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oyun.data.UserFriend
import com.example.oyun.databinding.ActivityFriendsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FriendsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFriendsBinding
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var adapter: FriendsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFriendsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadFriends()
        setupAddFriendButton()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(emptyList()) { friend ->
            inviteFriend(friend)
        }
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.friendsRecyclerView.adapter = adapter
    }

    private fun loadFriends() {
        val userId = auth.currentUser?.uid ?: return
        
        // Şimdilik dummy veri
        val dummyFriends = listOf(
            UserFriend("id1", "Ahmet"),
            UserFriend("id2", "Ayşe"),
            UserFriend("id3", "Mehmet")
        )
        adapter.updateList(dummyFriends)
    }

    private fun setupAddFriendButton() {
        binding.addFriendFab.setOnClickListener {
            showAddFriendDialog()
        }
    }

    private fun showAddFriendDialog() {
        val input = EditText(this)
        input.hint = "Kullanıcı Adı veya E-posta"
        
        AlertDialog.Builder(this)
            .setTitle("Arkadaş Ekle")
            .setView(input)
            .setPositiveButton("Ekle") { _, _ ->
                val query = input.text.toString()
                if (query.isNotEmpty()) {
                    sendFriendRequest(query)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun sendFriendRequest(query: String) {
        Toast.makeText(this, "İstek gönderildi: $query", Toast.LENGTH_SHORT).show()
    }

    private fun inviteFriend(friend: UserFriend) {
        Toast.makeText(this, "${friend.userName} davet edildi!", Toast.LENGTH_SHORT).show()
    }
}
