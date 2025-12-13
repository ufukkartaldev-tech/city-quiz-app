package com.example.oyun.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.oyun.data.remote.AuthRepository
import com.example.oyun.data.remote.GoogleSignInHelper
import com.example.oyun.databinding.ActivityAuthBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var googleSignInHelper: GoogleSignInHelper

    private lateinit var signInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Kullanıcı zaten giriş yaptıysa direkt ana menüye git
        if (authRepository.isUserLoggedIn()) {
            goToMainMenu()
            return
        }

        setupSignInLauncher()
        setupButtons()
        updateUI()
    }

    private fun setupSignInLauncher() {
        signInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("AuthActivity", "Sign-in result code: ${result.resultCode}")
            if (result.resultCode == RESULT_OK) {
                val data = result.data
                handleSignInResult(data)
            } else {
                Log.d("AuthActivity", "Sign-in cancelled or failed")
                Toast.makeText(this, "Giriş iptal edildi", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupButtons() {
        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }

        binding.skipButton.setOnClickListener {
            // Misafir olarak devam et
            goToMainMenu()
        }
    }

    private fun signInWithGoogle() {
        try {
            val signInIntent = googleSignInHelper.getSignInIntent(this)
            if (signInIntent != null) {
                signInLauncher.launch(signInIntent)
            } else {
                Toast.makeText(this, "Google Sign-In başlatılamadı", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AuthActivity", "Error starting sign-in", e)
            Toast.makeText(this, "Hata: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun handleSignInResult(data: Intent?) {
        try {
            Log.d("AuthActivity", "Handling sign-in result")
            val result = googleSignInHelper.handleSignInResult(data)

            if (result.isSuccess) {
                val idToken = result.getOrNull()
                Log.d("AuthActivity", "ID Token received: ${idToken != null}")
                
                if (idToken != null) {
                    lifecycleScope.launch {
                        try {
                            val authResult = authRepository.signInWithGoogle(idToken)
                            if (authResult.isSuccess) {
                                Toast.makeText(this@AuthActivity, "Giriş başarılı!", Toast.LENGTH_SHORT).show()
                                goToMainMenu()
                            } else {
                                val error = authResult.exceptionOrNull()?.message ?: "Bilinmeyen hata"
                                Log.e("AuthActivity", "Firebase auth failed: $error")
                                Toast.makeText(this@AuthActivity, "Firebase girişi başarısız, misafir olarak devam ediliyor", Toast.LENGTH_SHORT).show()
                                goToMainMenu()
                            }
                        } catch (e: Exception) {
                            Log.e("AuthActivity", "Exception during Firebase auth", e)
                            Toast.makeText(this@AuthActivity, "Hata oluştu, misafir olarak devam ediliyor", Toast.LENGTH_SHORT).show()
                            goToMainMenu()
                        }
                    }
                } else {
                    Log.e("AuthActivity", "ID token is null")
                    Toast.makeText(this, "ID token alınamadı, misafir olarak devam ediliyor", Toast.LENGTH_SHORT).show()
                    goToMainMenu()
                }
            } else {
                val error = result.exceptionOrNull()?.message ?: "Bilinmeyen hata"
                Log.e("AuthActivity", "Google sign-in failed: $error")
                Toast.makeText(this, "Google girişi başarısız: $error", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("AuthActivity", "Exception in handleSignInResult", e)
            Toast.makeText(this, "Beklenmeyen hata, misafir olarak devam ediliyor", Toast.LENGTH_SHORT).show()
            goToMainMenu()
        }
    }

    private fun updateUI() {
        val user = authRepository.getCurrentUser()
        if (user != null) {
            binding.welcomeText.text = "Hoş geldin, ${user.displayName ?: "Kullanıcı"}!"
            binding.googleSignInButton.setOnClickListener {
                signOut()
            }
        } else {
            binding.welcomeText.text = "Oyuna başlamak için giriş yapın"
            binding.googleSignInButton.setOnClickListener {
                signInWithGoogle()
            }
        }
    }

    private fun signOut() {
        authRepository.signOut()
        googleSignInHelper.signOut(this)
        updateUI()
        Toast.makeText(this, "Çıkış yapıldı", Toast.LENGTH_SHORT).show()
    }

    private fun goToMainMenu() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
