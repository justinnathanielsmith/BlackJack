package io.github.smithjustinn.ui.root

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.game.GameArgs
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class DeepLinkUtilsTest {
    @Test
    fun `valid deep link parses correctly`() {
        val url = "memorymatch://game?mode=TIME_ATTACK&pairs=8&seed=12345"
        val params = DeepLinkUtils.parseDeepLink(url)

        assertNotNull(params)
        assertEquals(GameMode.TIME_ATTACK, params.mode)
        assertEquals(8, params.pairs)
        assertEquals(12345L, params.seed)
    }

    @Test
    fun `malformed deep link returns defaults or null`() {
        // Missing query params -> defaults
        val url = "memorymatch://game"
        val params = DeepLinkUtils.parseDeepLink(url)
        assertNotNull(params)
        assertEquals(GameMode.TIME_ATTACK, params.mode)
        assertEquals(8, params.pairs) // Default
        assertNull(params.seed)

        // Invalid pairs
        val url2 = "memorymatch://game?pairs=notanumber"
        val params2 = DeepLinkUtils.parseDeepLink(url2)
        assertNotNull(params2)
        assertEquals(8, params2.pairs)

        // Invalid prefix -> null
        val url3 = "http://google.com"
        assertNull(DeepLinkUtils.parseDeepLink(url3))
    }

    @Test
    fun `deep link pairs are coerced`() {
        // Too small
        val url = "memorymatch://game?pairs=1"
        val params = DeepLinkUtils.parseDeepLink(url)
        assertNotNull(params)
        assertEquals(GameArgs.MIN_PAIRS, params.pairs) // 2

        // Too large
        val url2 = "memorymatch://game?pairs=100"
        val params2 = DeepLinkUtils.parseDeepLink(url2)
        assertNotNull(params2)
        assertEquals(GameArgs.MAX_PAIRS, params2.pairs) // 26
    }

    @Test
    fun `deep link with extra params works`() {
        val url = "memorymatch://game?mode=DAILY_CHALLENGE&pairs=12&extra=foo"
        val params = DeepLinkUtils.parseDeepLink(url)
        assertNotNull(params)
        assertEquals(GameMode.DAILY_CHALLENGE, params.mode)
        assertEquals(12, params.pairs)
    }

    @Test
    fun `deep link with encoded chars works`() {
        // Encoded & should not split params
        val url = "memorymatch://game?mode=TIME_ATTACK&pairs=8&comment=hello%26world"
        val params = DeepLinkUtils.parseDeepLink(url)
        assertNotNull(params)
        assertEquals(GameMode.TIME_ATTACK, params.mode)
        assertEquals(8, params.pairs)
    }

    @Test
    fun `deep link with similar keys works`() {
        // 'xmode' vs 'mode'
        val url = "memorymatch://game?xmode=TIME_ATTACK&mode=DAILY_CHALLENGE"
        val params = DeepLinkUtils.parseDeepLink(url)
        assertNotNull(params)
        assertEquals(GameMode.DAILY_CHALLENGE, params.mode)
    }

    @Test
    fun `sanitize deep link redacts query params`() {
        val url = "memorymatch://game?mode=TIME_ATTACK&pairs=8&seed=12345"
        val sanitized = DeepLinkUtils.sanitizeForLogging(url)
        assertEquals("memorymatch://game?mode=REDACTED&pairs=REDACTED&seed=REDACTED", sanitized)
    }

    @Test
    fun `sanitize invalid url returns fallback`() {
        val url = ":"
        val sanitized = DeepLinkUtils.sanitizeForLogging(url)
        assertEquals("Malformatted Deep Link", sanitized)
    }

    @Test
    fun `sanitize deep link with no query params returns original`() {
        val url = "memorymatch://game"
        val sanitized = DeepLinkUtils.sanitizeForLogging(url)
        assertEquals("memorymatch://game", sanitized)
    }

    @Test
    fun `sanitizeForLogging should redact sensitive data in path`() {
        val sensitiveUrl = "memorymatch://game/userToken12345?mode=TIME_ATTACK"
        val sanitized = DeepLinkUtils.sanitizeForLogging(sensitiveUrl)
        // Path should be stripped, params redacted
        assertEquals("memorymatch://game?mode=REDACTED", sanitized)
    }
}
