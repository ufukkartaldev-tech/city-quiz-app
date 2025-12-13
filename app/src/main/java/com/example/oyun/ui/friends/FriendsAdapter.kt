package com.example.oyun.ui.friends

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.data.UserFriend
import com.example.oyun.databinding.ItemFriendBinding

class FriendsAdapter(
    private var friends: List<UserFriend>,
    private val onInviteClick: (UserFriend) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(private val binding: ItemFriendBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(friend: UserFriend) {
            binding.friendName.text = friend.userName
            binding.inviteButton.setOnClickListener { onInviteClick(friend) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val binding = ItemFriendBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FriendViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        holder.bind(friends[position])
    }

    override fun getItemCount() = friends.size

    fun updateList(newList: List<UserFriend>) {
        friends = newList
        notifyDataSetChanged()
    }
}
