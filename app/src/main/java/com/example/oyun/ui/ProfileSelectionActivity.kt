package com.example.oyun.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.oyun.databinding.ActivityProfileSelectionBinding
import dagger.hilt.android.AndroidEntryPoint // HİLT'İN KİMLİK KARTINI İMPORT EDİYORUZ
import javax.inject.Inject // USTA ÇAĞIRMA KOMUTUNU İMPORT EDİYORUZ

// BU ETİKET, "BU ODA ARTIK USTA ÇAĞIRMA MERKEZİNDEN HİZMET ALABİLİR" DEMEKTİR.
@AndroidEntryPoint
class ProfileSelectionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileSelectionBinding

    // --- USTA ÇAĞIRMA BÖLÜMÜ ---
    // ARTIK "getSharedPreferences(...)" YOK! SADECE İSTİYORUZ, HILT GETİRİYOR.
    @Inject
    lateinit var prefs: SharedPreferences

    private lateinit var profiles: MutableList<String>
    private lateinit var profileAdapter: ProfileAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileSelectionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // --- BÜTÜN AMELELİK SİLİNDİ! ---
        // Eskiden burada `prefs = getSharedPreferences(...)` diye bir satır vardı.
        // Artık Hilt bu işi bizim için arka planda, daha verimli bir şekilde yapıyor.

        loadProfiles()
        setupRecyclerView()

        binding.addProfileButton.setOnClickListener {
            addNewProfile()
        }
    }

    private fun loadProfiles() {
        val profilesString = prefs.getString("all_users", null)
        profiles = if (profilesString.isNullOrEmpty()) {
            mutableListOf("Misafir") // Hiç profil yoksa Misafir'i ekleyerek başlayalım.
        } else {
            profilesString.split(",").toMutableList()
        }
    }

    private fun setupRecyclerView() {
        profileAdapter = ProfileAdapter(
            profiles = profiles,
            onProfileClicked = { selectedProfile ->
                setActiveUser(selectedProfile)
                navigateToMain()
            },
            onProfileLongClicked = { profileToDelete ->
                showDeleteConfirmDialog(profileToDelete)
            }
        )
        binding.profilesRecyclerView.adapter = profileAdapter
        binding.profilesRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun showDeleteConfirmDialog(profileName: String) {
        if (profileName == "Misafir") {
            Toast.makeText(this, "Misafir profili silinemez!", Toast.LENGTH_SHORT).show()
            return
        }
        if (profiles.size <= 1) {
            Toast.makeText(this, "Son profili silemezsiniz!", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(this)
            .setTitle("Profil Sil")
            .setMessage("'$profileName' profilini silmek istediğinizden emin misiniz?\n\nTüm veriler (seviye, jokerler, başarımlar) silinecek!")
            .setPositiveButton("Sil") { _, _ ->
                deleteProfile(profileName)
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun deleteProfile(profileName: String) {
        profiles.remove(profileName)
        saveProfiles()
        profileAdapter.notifyDataSetChanged()
        deleteProfileData(profileName)

        val currentActiveUser = prefs.getString("last_active_user", "Misafir")
        if (currentActiveUser == profileName) {
            setActiveUser(profiles.firstOrNull() ?: "Misafir")
        }
        Toast.makeText(this, "'$profileName' profili silindi", Toast.LENGTH_SHORT).show()
    }

    private fun deleteProfileData(profileName: String) {
        val editor = prefs.edit()
        val allKeys = prefs.all.keys
        allKeys.forEach { key ->
            if (key.startsWith("profile_${profileName}_") || key.startsWith("daily_${profileName}_")) {
                editor.remove(key)
            }
        }
        editor.apply()
    }

    private fun addNewProfile() {
        val newName = binding.newProfileNameEditText.text.toString().trim()

        if (newName.isEmpty()) {
            Toast.makeText(this, "Profil adı boş olamaz!", Toast.LENGTH_SHORT).show()
            return
        }
        if (newName.length > 15) {
            Toast.makeText(this, "Profil adı en fazla 15 karakter olabilir!", Toast.LENGTH_SHORT).show()
            return
        }
        if (profiles.any { it.equals(newName, ignoreCase = true) }) {
            Toast.makeText(this, "Bu isimde bir profil zaten mevcut!", Toast.LENGTH_SHORT).show()
            return
        }
        if (profiles.size >= 10) {
            Toast.makeText(this, "En fazla 10 profil oluşturabilirsiniz!", Toast.LENGTH_SHORT).show()
            return
        }

        profiles.add(newName)
        saveProfiles()
        profileAdapter.notifyItemInserted(profiles.size - 1)
        binding.newProfileNameEditText.text.clear()

        setActiveUser(newName)
        navigateToMain()
    }

    private fun saveProfiles() {
        val profilesString = profiles.joinToString(",")
        prefs.edit().putString("all_users", profilesString).apply()
    }

    private fun setActiveUser(username: String) {
        prefs.edit().putString("last_active_user", username).apply()
    }

    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
