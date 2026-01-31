package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CardStateTest {
    @Test
    fun testDefaults() {
        val card =
            CardState(
                id = 1,
                suit = Suit.Hearts,
                rank = Rank.Ace,
            )
        assertEquals(1, card.id)
        assertEquals(Suit.Hearts, card.suit)
        assertEquals(Rank.Ace, card.rank)
        assertFalse(card.isFaceUp)
        assertFalse(card.isMatched)
        assertFalse(card.isError)
    }

    @Test
    fun testSuits() {
        assertTrue(Suit.Hearts.isRed)
        assertTrue(Suit.Diamonds.isRed)
        assertFalse(Suit.Clubs.isRed)
        assertFalse(Suit.Spades.isRed)
        assertEquals("♥", Suit.Hearts.symbol)
    }

    @Test
    fun testRanks() {
        assertEquals("A", Rank.Ace.symbol)
        assertEquals("K", Rank.King.symbol)
    }
}
