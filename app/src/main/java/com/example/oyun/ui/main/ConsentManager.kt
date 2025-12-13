package com.example.oyun.ui.main

import android.content.Context
import android.util.Log
import com.google.android.gms.ads.MobileAds
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform

/**
 * GDPR rıza yönetimini sağlayan sınıf
 */
class ConsentManager(
    private val context: Context,
    private val onConsentComplete: () -> Unit
) {

    private var isMobileAdsInitialized = false

    fun requestConsent() {
        val params = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

        val consentInformation = UserMessagingPlatform.getConsentInformation(context)

        // Test için gerekirse rıza sıfırlanabilir:
        // consentInformation.reset()

        consentInformation.requestConsentInfoUpdate(
            context as android.app.Activity,
            params,
            {
                loadAndShowConsentFormIfRequired(consentInformation)
            },
            { error ->
                Log.w(TAG, "Consent info update error: ${error.errorCode} ${error.message}")
            }
        )
    }

    private fun loadAndShowConsentFormIfRequired(consentInformation: ConsentInformation) {
        UserMessagingPlatform.loadAndShowConsentFormIfRequired(
            context as android.app.Activity
        ) { loadAndShowError ->
            if (loadAndShowError != null) {
                Log.w(TAG, "Consent form error: ${loadAndShowError.errorCode} ${loadAndShowError.message}")
            }

            // Rıza durumu ne olursa olsun reklamları başlat
            if (consentInformation.canRequestAds()) {
                initializeMobileAds()
            }
        }
    }

    private fun initializeMobileAds() {
        if (isMobileAdsInitialized) return
        isMobileAdsInitialized = true

        MobileAds.initialize(context) {}
        onConsentComplete()
        Log.d(TAG, "Mobile Ads initialized")
    }

    companion object {
        private const val TAG = "ConsentManager"
    }
}
