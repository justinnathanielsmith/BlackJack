package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CardThemeTest {

    @Test
    fun testCardBackThemeFromIdOrName() {
        // Exact ID match
        assertEquals(CardBackTheme.GEOMETRIC, CardBackTheme.fromIdOrName("theme_standard"))
        assertEquals(CardBackTheme.PATTERN, CardBackTheme.fromIdOrName("theme_pattern"))

        // Exact name match
        assertEquals(CardBackTheme.CLASSIC, CardBackTheme.fromIdOrName("CLASSIC"))
        assertEquals(CardBackTheme.POKER, CardBackTheme.fromIdOrName("POKER"))

        // Value starting with ID
        assertEquals(CardBackTheme.GEOMETRIC, CardBackTheme.fromIdOrName("theme_standard_suffix"))
        assertEquals(CardBackTheme.PATTERN, CardBackTheme.fromIdOrName("theme_pattern_other"))

        // Fallback to GEOMETRIC for invalid inputs
        assertEquals(CardBackTheme.GEOMETRIC, CardBackTheme.fromIdOrName("invalid_theme"))
        assertEquals(CardBackTheme.GEOMETRIC, CardBackTheme.fromIdOrName(""))
    }

    @Test
    fun testCardSymbolThemeFromIdOrName() {
        // Exact ID match
        assertEquals(CardSymbolTheme.CLASSIC, CardSymbolTheme.fromIdOrName("skin_classic"))
        assertEquals(CardSymbolTheme.PIXEL, CardSymbolTheme.fromIdOrName("skin_pixel"))

        // Exact name match
        assertEquals(CardSymbolTheme.MINIMAL, CardSymbolTheme.fromIdOrName("MINIMAL"))
        assertEquals(CardSymbolTheme.NEON, CardSymbolTheme.fromIdOrName("NEON"))

        // Fallback to CLASSIC for invalid inputs (including startsWith which shouldn't work here)
        assertEquals(CardSymbolTheme.CLASSIC, CardSymbolTheme.fromIdOrName("skin_pixel_suffix"))
        assertEquals(CardSymbolTheme.CLASSIC, CardSymbolTheme.fromIdOrName("invalid_skin"))
        assertEquals(CardSymbolTheme.CLASSIC, CardSymbolTheme.fromIdOrName(""))
    }

    @Test
    fun testCardThemeDefaults() {
        val theme = CardTheme()
        assertEquals(CardBackTheme.GEOMETRIC, theme.back)
        assertEquals(CardSymbolTheme.CLASSIC, theme.skin)
        assertNull(theme.backColorHex)

        val customTheme = CardTheme(
            back = CardBackTheme.POKER,
            skin = CardSymbolTheme.CYBERPUNK,
            backColorHex = "#FFFFFF"
        )
        assertEquals(CardBackTheme.POKER, customTheme.back)
        assertEquals(CardSymbolTheme.CYBERPUNK, customTheme.skin)
        assertEquals("#FFFFFF", customTheme.backColorHex)
    }
}
