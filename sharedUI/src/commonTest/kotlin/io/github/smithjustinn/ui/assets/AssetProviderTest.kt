package io.github.smithjustinn.ui.assets

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AssetProviderTest {
    @Test
    fun `getBackTheme returns correct theme for valid IDs`() {
        assertEquals(CardBackTheme.GEOMETRIC, AssetProvider.getBackTheme("theme_standard"))
        assertEquals(CardBackTheme.CLASSIC, AssetProvider.getBackTheme("theme_classic"))
        assertEquals(CardBackTheme.POKER, AssetProvider.getBackTheme("theme_poker"))
    }

    @Test
    fun `getBackTheme returns null for invalid ID`() {
        assertNull(AssetProvider.getBackTheme("invalid_id"))
    }

    @Test
    fun `getBackTheme resolves base theme for variant IDs`() {
        assertEquals(CardBackTheme.GEOMETRIC, AssetProvider.getBackTheme("theme_standard_green"))
        assertEquals(CardBackTheme.GEOMETRIC, AssetProvider.getBackTheme("theme_standard_charcoal"))
        assertEquals(CardBackTheme.CLASSIC, AssetProvider.getBackTheme("theme_classic_blue"))
        assertEquals(CardBackTheme.POKER, AssetProvider.getBackTheme("theme_poker_red"))
    }

    @Test
    fun `getSymbolTheme returns correct theme for valid IDs`() {
        assertEquals(CardSymbolTheme.CLASSIC, AssetProvider.getSymbolTheme("skin_classic"))
        assertEquals(CardSymbolTheme.MINIMAL, AssetProvider.getSymbolTheme("skin_minimal"))
        assertEquals(CardSymbolTheme.TEXT_ONLY, AssetProvider.getSymbolTheme("skin_text"))
        assertEquals(CardSymbolTheme.POKER, AssetProvider.getSymbolTheme("skin_poker"))
    }

    @Test
    fun `getSymbolTheme returns null for invalid ID`() {
        assertNull(AssetProvider.getSymbolTheme("invalid_id"))
    }

    @Test
    fun `all shop item IDs resolve to valid themes`() {
        // Test all known back theme IDs
        val backThemeIds = listOf("theme_standard", "theme_classic", "theme_poker")
        backThemeIds.forEach { id ->
            assertNotNull(AssetProvider.getBackTheme(id), "Back theme ID '$id' should resolve")
        }

        // Test all known symbol theme IDs
        val symbolThemeIds = listOf("skin_classic", "skin_minimal", "skin_text", "skin_poker")
        symbolThemeIds.forEach { id ->
            assertNotNull(AssetProvider.getSymbolTheme(id), "Symbol theme ID '$id' should resolve")
        }
    }
}
