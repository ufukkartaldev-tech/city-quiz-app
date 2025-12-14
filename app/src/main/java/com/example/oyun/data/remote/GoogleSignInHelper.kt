package com.example.oyun.data.remote

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSignInHelper @Inject constructor() {

    companion object {
        private const val RC_SIGN_IN = 9001
    }

    private var googleSignInClient: GoogleSignInClient? = null

    fun getSignInIntent(activity: Activity): Intent? {
        if (googleSignInClient == null) {
            // Get Web Client ID from google-services.json
            val webClientId = try {
                activity.getString(com.example.oyun.R.string.default_web_client_id)
            } catch (e: Exception) {
                // Fallback to hardcoded (NOT RECOMMENDED for production)
                android.util.Log.e("GoogleSignInHelper", "Failed to get web client ID from resources", e)
                "736807627314-fvf2irai5bh9k92obl5the9cp2vds98c.apps.googleusercontent.com"
            }
            
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(webClientId)
                .requestEmail()
                .build()

            googleSignInClient = GoogleSignIn.getClient(activity, gso)
        }

        return googleSignInClient?.signInIntent
    }

    fun handleSignInResult(data: Intent?): Result<String> {
        return try {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account: GoogleSignInAccount = task.getResult(ApiException::class.java)
            val idToken = account.idToken

            if (idToken != null) {
                Result.success(idToken)
            } else {
                Result.failure(Exception("ID token is null"))
            }
        } catch (e: ApiException) {
            Result.failure(Exception("Google sign-in failed: ${e.statusCode}"))
        }
    }

    fun signOut(activity: Activity) {
        googleSignInClient?.signOut()
    }

    fun getRequestCode(): Int = RC_SIGN_IN
}
