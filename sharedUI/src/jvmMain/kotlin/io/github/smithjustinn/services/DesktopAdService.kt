package io.github.smithjustinn.services

import io.github.smithjustinn.domain.services.AdService

/**
 * Desktop (JVM) no-op implementation of AdService.
 * Rewarded ads are not supported on desktop.
 */
class DesktopAdService : AdService {
    override fun loadRewardedAd(adUnitId: String) {
        // No-op: Desktop doesn't support ads
    }

    override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
        // No-op: Desktop doesn't support ads
    }
}
