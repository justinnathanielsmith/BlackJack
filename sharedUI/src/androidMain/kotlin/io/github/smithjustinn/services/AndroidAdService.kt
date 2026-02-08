package io.github.smithjustinn.services

import android.app.Activity
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import io.github.smithjustinn.domain.services.AdService

/**
 * Android implementation of AdService using Google Mobile Ads SDK.
 */
class AndroidAdService(
    private val activityProvider: () -> Activity?,
) : AdService {
    private var rewardedAd: RewardedAd? = null

    override fun loadRewardedAd(adUnitId: String) {
        val activity = activityProvider() ?: return
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            activity,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                }
            },
        )
    }

    override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
        val activity = activityProvider() ?: return
        rewardedAd?.show(activity) { rewardItem ->
            onRewardEarned(rewardItem.amount)
        }
    }
}
