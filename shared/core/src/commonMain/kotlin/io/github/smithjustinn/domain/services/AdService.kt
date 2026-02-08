package io.github.smithjustinn.domain.services

/**
 * Platform-specific service for loading and showing rewarded ads.
 */
interface AdService {
    /**
     * Loads a rewarded ad with the given ad unit ID.
     * @param adUnitId The platform-specific ad unit identifier
     */
    fun loadRewardedAd(adUnitId: String)

    /**
     * Shows the loaded rewarded ad.
     * @param onRewardEarned Callback invoked when the user earns the reward (bonus seconds)
     */
    fun showRewardedAd(onRewardEarned: (Int) -> Unit)
}
