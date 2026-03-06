package io.github.smithjustinn.services

import kotlin.test.Test
import kotlin.test.assertFalse

class DesktopAdServiceTest {
    @Test
    fun loadRewardedAdDoesNotThrow() {
        // Desktop implementation is a no-op, just verify it doesn't crash
        val service = DesktopAdService()
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
        val service = DesktopAdService()
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
        assertFalse(callbackInvoked, "Callback should not be invoked on Desktop since ads are not supported")
    }
}
