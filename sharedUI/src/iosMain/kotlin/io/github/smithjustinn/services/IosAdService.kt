package io.github.smithjustinn.services

import co.touchlab.kermit.Logger
import io.github.smithjustinn.domain.services.AdService

/**
 * iOS implementation of AdService.
 * NOTE: Currently a stub because Google Mobile Ads SDK requires CocoaPods setup
 * which is not available in the current environment.
 */
class IosAdService : AdService {
    private val logger = Logger.withTag("IosAdService")

    override fun loadRewardedAd(adUnitId: String) {
        logger.i { "Rewarded ad load requested for $adUnitId (Stub)" }
    }

    override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
        logger.i { "Rewarded ad show requested (Stub)" }
        // For development/stub purposes, we can either do nothing or provide a mock reward
        // onRewardEarned(10)
    }
}
