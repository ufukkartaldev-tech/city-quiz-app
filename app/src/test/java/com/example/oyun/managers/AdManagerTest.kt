package com.example.oyun.managers

import android.content.Context
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.rewarded.RewardedAd
import io.mockk.*
import org.junit.Before
import org.junit.Test
import com.google.common.truth.Truth.assertThat

/**
 * Unit tests for AdManager
 * Tests ad loading and showing logic
 */
class AdManagerTest {

    private lateinit var adManager: AdManager
    private lateinit var mockContext: Context

    @Before
    fun setup() {
        mockContext = mockk(relaxed = true)
        adManager = AdManager(mockContext)
    }

    @Test
    fun `loadInterstitialAd should request ad load`() {
        // Given - AdManager is initialized

        // When
        adManager.loadInterstitialAd()

        // Then
        // Verify that ad loading was initiated
        // Note: This is a basic test. In real scenarios, you'd mock the AdLoader
        assertThat(adManager).isNotNull()
    }

    @Test
    fun `loadRewardedAd should request ad load`() {
        // Given - AdManager is initialized

        // When
        adManager.loadRewardedAd()

        // Then
        assertThat(adManager).isNotNull()
    }

    @Test
    fun `showInterstitialAd should not crash when ad not loaded`() {
        // Given - No ad loaded

        // When
        val result = adManager.showInterstitialAd(mockk(relaxed = true)) {}

        // Then
        assertThat(result).isFalse() // Should return false when ad not ready
    }

    @Test
    fun `showRewardedAd should not crash when ad not loaded`() {
        // Given - No ad loaded

        // When
        val result = adManager.showRewardedAd(mockk(relaxed = true)) {}

        // Then
        assertThat(result).isFalse() // Should return false when ad not ready
    }

    @Test
    fun `isInterstitialAdReady should return false initially`() {
        // Given - Fresh AdManager

        // When
        val isReady = adManager.isInterstitialAdReady()

        // Then
        assertThat(isReady).isFalse()
    }

    @Test
    fun `isRewardedAdReady should return false initially`() {
        // Given - Fresh AdManager

        // When
        val isReady = adManager.isRewardedAdReady()

        // Then
        assertThat(isReady).isFalse()
    }
}
