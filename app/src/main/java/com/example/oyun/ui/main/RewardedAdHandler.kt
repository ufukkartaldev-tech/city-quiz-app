package com.example.oyun.ui.main

import android.content.Context
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.oyun.R
import com.example.oyun.managers.AdManager

/**
 * Ödüllü reklam işlemlerini yöneten sınıf
 */
class RewardedAdHandler(
    private val context: Context,
    private val adManager: AdManager,
    private val profileManager: ProfileManager,
    private val onRewardGranted: () -> Unit
) {

    fun showRewardedAdDialog() {
        AlertDialog.Builder(context)
            .setTitle(context.getString(R.string.watch_ad_title))
            .setMessage(context.getString(R.string.watch_ad_message))
            .setPositiveButton(context.getString(R.string.watch_ad_btn)) { _, _ ->
                showRewardedAd()
            }
            .setNegativeButton(context.getString(R.string.cancel), null)
            .create()
            .show()
    }

    private fun showRewardedAd() {
        if (adManager.isRewardedAdReady()) {
            adManager.showRewardedAd(
                context as android.app.Activity,
                onRewarded = {
                    grantJokerRewards()
                },
                onAdClosed = {
                    // Reklam kapandı
                }
            )
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.ad_not_ready),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun grantJokerRewards() {
        profileManager.addJokerRewards(
            fiftyFifty = JOKER_REWARD_COUNT,
            skip = JOKER_REWARD_COUNT,
            gainLife = JOKER_REWARD_COUNT
        )
        
        Toast.makeText(
            context,
            context.getString(R.string.ad_reward_success),
            Toast.LENGTH_LONG
        ).show()
        
        onRewardGranted()
    }

    companion object {
        private const val JOKER_REWARD_COUNT = 2
    }
}
