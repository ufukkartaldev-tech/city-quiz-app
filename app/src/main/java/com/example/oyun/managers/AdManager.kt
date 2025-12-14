package com.example.oyun.managers

import android.app.Activity
import android.content.Context
import android.util.Log
import com.google.android.gms.ads.*
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdManager @Inject constructor(
    private val context: Context
) {

    companion object {
        // Test ID'leri (Google'ın sunduğu standart test ID'leri)
        private const val TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
        private const val TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
        private const val TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"

        // Gerçek ID'ler
        private const val PROD_BANNER_ID = "ca-app-pub-1334433458655438/1398319482"
        private const val PROD_INTERSTITIAL_ID = "ca-app-pub-1334433458655438/7748121326"
        private const val PROD_REWARDED_ID = "ca-app-pub-1334433458655438/6975640297"

        // Build tipine göre otomatik seçim
        val BANNER_AD_UNIT_ID: String
            get() = if (com.example.oyun.BuildConfig.DEBUG) TEST_BANNER_ID else PROD_BANNER_ID

        val INTERSTITIAL_AD_UNIT_ID: String
            get() = if (com.example.oyun.BuildConfig.DEBUG) TEST_INTERSTITIAL_ID else PROD_INTERSTITIAL_ID

        val REWARDED_AD_UNIT_ID: String
            get() = if (com.example.oyun.BuildConfig.DEBUG) TEST_REWARDED_ID else PROD_REWARDED_ID
    }

    private var interstitialAd: InterstitialAd? = null
    private var rewardedAd: RewardedAd? = null
    private var bannerAdView: AdView? = null

    init {
        // AdMob'u başlat
        MobileAds.initialize(context) { initializationStatus ->
            Log.d("AdManager", "AdMob initialized: $initializationStatus")
        }

        // Reklamları önceden yükle
        loadInterstitialAd()
        loadRewardedAd()
    }

    // ==================== BANNER REKLAM ====================

    fun createBannerAdView(): AdView {
        bannerAdView = AdView(context).apply {
            setAdSize(AdSize.BANNER)
            adUnitId = BANNER_AD_UNIT_ID
            loadAd(AdRequest.Builder().build())
        }
        return bannerAdView!!
    }

    fun destroyBannerAd() {
        bannerAdView?.destroy()
        bannerAdView = null
    }

    // ==================== GEÇİŞ REKLAMI ====================

    private fun loadInterstitialAd() {
        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(context, INTERSTITIAL_AD_UNIT_ID, adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    Log.d("AdManager", "Interstitial ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Interstitial ad failed to load: ${error.message}")
                    interstitialAd = null
                }
            })
    }

    fun showInterstitialAd(activity: Activity, onAdClosed: () -> Unit = {}) {
        if (interstitialAd != null) {
            interstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdManager", "Interstitial ad dismissed")
                    loadInterstitialAd() // Yeni reklam yükle
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e("AdManager", "Interstitial ad failed to show: ${error.message}")
                    loadInterstitialAd()
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdManager", "Interstitial ad showed")
                    interstitialAd = null
                }
            }
            interstitialAd?.show(activity)
        } else {
            Log.w("AdManager", "Interstitial ad not ready")
            onAdClosed()
        }
    }

    fun isInterstitialAdReady(): Boolean = interstitialAd != null

    // ==================== ÖDÜLLÜ REKLAM ====================

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(context, REWARDED_AD_UNIT_ID, adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    Log.d("AdManager", "Rewarded ad loaded")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.e("AdManager", "Rewarded ad failed to load: ${error.message}")
                    rewardedAd = null
                }
            })
    }

    fun showRewardedAd(
        activity: Activity,
        onRewarded: () -> Unit,
        onAdClosed: () -> Unit = {}
    ) {
        if (rewardedAd != null) {
            rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d("AdManager", "Rewarded ad dismissed")
                    loadRewardedAd()
                    onAdClosed()
                }

                override fun onAdFailedToShowFullScreenContent(error: AdError) {
                    Log.e("AdManager", "Rewarded ad failed to show: ${error.message}")
                    loadRewardedAd()
                    onAdClosed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.d("AdManager", "Rewarded ad showed")
                }
            }

            rewardedAd?.show(activity) { rewardItem ->
                Log.d("AdManager", "User rewarded: ${rewardItem.amount} ${rewardItem.type}")
                onRewarded()
            }
        } else {
            Log.w("AdManager", "Rewarded ad not ready")
        }
    }

    fun isRewardedAdReady(): Boolean = rewardedAd != null

    // ==================== GENEL ====================

    fun destroy() {
        destroyBannerAd()
        interstitialAd = null
        rewardedAd = null
    }
}
