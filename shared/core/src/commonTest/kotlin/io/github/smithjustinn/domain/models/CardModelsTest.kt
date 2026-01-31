package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardModelsTest {

    @Test
    fun `Suit isRed returns correct values`() {
        assertTrue(Suit.Hearts.isRed)
        assertTrue(Suit.Diamonds.isRed)
        assertFalse(Suit.Clubs.isRed)
        assertFalse(Suit.Spades.isRed)
    }

    @Test
    fun `Suit symbols are correct`() {
        assertEquals("♥", Suit.Hearts.symbol)
        assertEquals("♦", Suit.Diamonds.symbol)
        assertEquals("♣", Suit.Clubs.symbol)
        assertEquals("♠", Suit.Spades.symbol)
    }

    @Test
    fun `Rank symbols are correct`() {
        assertEquals("A", Rank.Ace.symbol)
        assertEquals("K", Rank.King.symbol)
        assertEquals("10", Rank.Ten.symbol)
    }
}
