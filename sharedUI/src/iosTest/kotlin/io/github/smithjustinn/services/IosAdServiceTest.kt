package io.github.smithjustinn.services

import kotlin.test.Test
import kotlin.test.assertFalse

class IosAdServiceTest {
    @Test
    fun loadRewardedAdDoesNotThrow() {
        // iOS implementation is currently a stub, ensure it runs without error
        val service = IosAdService()
        var exceptionThrown = false

        try {
            service.loadRewardedAd("test_ad_unit_123")
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertFalse(exceptionThrown, "loadRewardedAd should not throw an exception")
    }

    @Test
    fun showRewardedAdDoesNotThrowAndDoesNotInvokeCallback() {
        val service = IosAdService()
        var exceptionThrown = false
        var callbackInvoked = false

        try {
            service.showRewardedAd {
                callbackInvoked = true
            }
        } catch (e: Exception) {
            exceptionThrown = true
        }

        assertFalse(exceptionThrown, "showRewardedAd should not throw an exception")
        assertFalse(callbackInvoked, "Callback should not be invoked for the current stub implementation")
    }
}
