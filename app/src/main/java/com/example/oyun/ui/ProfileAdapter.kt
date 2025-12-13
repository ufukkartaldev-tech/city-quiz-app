package com.example.oyun.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.oyun.databinding.ItemProfileBinding

class ProfileAdapter(
    private val profiles: List<String>,
    private val onProfileClicked: (String) -> Unit,
    private val onProfileLongClicked: (String) -> Unit
) : RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder>() {

    class ProfileViewHolder(val binding: ItemProfileBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        val binding = ItemProfileBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProfileViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val profileName = profiles[position]
        
        with(holder.binding) {
            profileNameText.text = profileName
            
            root.setOnClickListener {
                onProfileClicked(profileName)
            }
            
            root.setOnLongClickListener {
                onProfileLongClicked(profileName)
                true
            }
        }
    }

    override fun getItemCount() = profiles.size
}
