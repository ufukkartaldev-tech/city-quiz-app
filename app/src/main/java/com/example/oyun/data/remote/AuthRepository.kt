package com.example.oyun.data.remote

import android.content.SharedPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val prefs: SharedPreferences
) {

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user

            if (user != null) {
                // Kullanıcı adını SharedPreferences'a kaydet
                val displayName = user.displayName ?: "Google User"
                prefs.edit().putString("last_active_user", displayName).apply()
                Result.success(user)
            } else {
                Result.failure(Exception("Google sign-in failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        prefs.edit().putString("last_active_user", "Misafir").apply()
    }

    fun getUserDisplayName(): String {
        return auth.currentUser?.displayName ?: "Misafir"
    }

    fun getActiveUser(): String {
        return prefs.getString("last_active_user", "Misafir") ?: "Misafir"
    }

    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    fun getUserPhotoUrl(): String? {
        return auth.currentUser?.photoUrl?.toString()
    }
}
