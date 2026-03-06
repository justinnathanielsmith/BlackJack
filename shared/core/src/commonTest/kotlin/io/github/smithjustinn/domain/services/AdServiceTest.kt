package io.github.smithjustinn.domain.services

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AdServiceTest {
    private class FakeAdService : AdService {
        var loadedAdUnitId: String? = null
        var isShowRequested: Boolean = false
        var rewardAmountToGive: Int = 0

        override fun loadRewardedAd(adUnitId: String) {
            loadedAdUnitId = adUnitId
        }

        override fun showRewardedAd(onRewardEarned: (Int) -> Unit) {
            isShowRequested = true
            if (rewardAmountToGive > 0) {
                onRewardEarned(rewardAmountToGive)
            }
        }
    }

    @Test
    fun testLoadRewardedAd() {
        // Arrange
        val fakeService = FakeAdService()
        val testAdUnitId = "test_ad_unit_123"

        // Act
        fakeService.loadRewardedAd(testAdUnitId)

        // Assert
        assertEquals(testAdUnitId, fakeService.loadedAdUnitId, "The ad unit ID should match what was loaded")
    }

    @Test
    fun testShowRewardedAdWithoutReward() {
        // Arrange
        val fakeService = FakeAdService()
        var rewardEarned = false

        // Act
        fakeService.showRewardedAd {
            rewardEarned = true
        }

        // Assert
        assertTrue(fakeService.isShowRequested, "Show request should be registered")
        assertFalse(rewardEarned, "Reward should not be earned if not provided")
    }

    @Test
    fun testShowRewardedAdWithReward() {
        // Arrange
        val fakeService = FakeAdService()
        fakeService.rewardAmountToGive = 15
        var earnedRewardAmount = 0

        // Act
        fakeService.showRewardedAd { amount ->
            earnedRewardAmount = amount
        }

        // Assert
        assertTrue(fakeService.isShowRequested, "Show request should be registered")
        assertEquals(15, earnedRewardAmount, "The correct reward amount should be returned via callback")
    }
}
