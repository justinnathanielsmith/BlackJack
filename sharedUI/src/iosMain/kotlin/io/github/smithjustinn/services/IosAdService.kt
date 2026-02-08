package io.github.smithjustinn.services

import io.github.smithjustinn.domain.services.AdService
import platform.Foundation.NSError
import platform.UIKit.UIApplication
import platform.GoogleMobileAds.GADRequest
import platform.GoogleMobileAds.GADRewardedAd
import platform.GoogleMobileAds.GADRewardedAdLoadCompletionHandler

/**
 * iOS implementation of AdService using Google Mobile Ads SDK.
 */
class IosAdService : AdService {
    private var rewardedAd: GADRewardedAd? = null

    override fun loadRewardedAd(adUnitId: String) {
        GADRewardedAd.loadWithAdUnitID(
            adUnitID = adUnitId,
            request = GADRequest(),
            completionHandler = { ad: GADRewardedAd?, error: NSError? ->
                if (error == null) {
                    rewardedAd = ad
                }
            } as GADRewardedAdLoadCompletionHandler,
        )
    }

    override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        if (rootVC != null && rewardedAd != null) {
            rewardedAd?.presentFromRootViewController(
                rootViewController = rootVC,
                userDidEarnRewardHandler = {
                    val reward = rewardedAd?.adReward
                    onRewardEarned(reward?.amount?.toInt() ?: 0)
                },
            )
        }
    }
}
