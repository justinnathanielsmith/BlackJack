package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MatchEvaluatorTest {
    @Test
    fun `handleMatchSuccess handles integer overflow safely`() {
        val highCombo = 50000

        // Ensure same suit and rank for a successful match
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = false)
        val card3 = CardState(id = 3, suit = Suit.Clubs, rank = Rank.King, isFaceUp = false)
        val card4 = CardState(id = 4, suit = Suit.Spades, rank = Rank.King, isFaceUp = false)

        val initialState =
            MemoryGameState(
                cards = persistentListOf(card1, card2, card3, card4),
                comboMultiplier = highCombo,
                currentPot = 100,
                score = 1000,
                pairCount = 2,
            )

        val (newState, _) = MatchEvaluator.flipCard(initialState, card2.id)

        assertTrue(newState.currentPot >= 0, "Current pot overflowed to negative value: ${newState.currentPot}")
        assertEquals(Int.MAX_VALUE, newState.currentPot, "Current pot should be capped at Int.MAX_VALUE")
    }

    @Test
    fun flipCard_shouldFindCard_whenIdsAreSwapped() {
        val card1 = CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 1, suit = Suit.Spades, rank = Rank.King)

        // Swap cards: Index 0 has ID 1, Index 1 has ID 0
        val cards = persistentListOf(card2, card1)

        val state = MemoryGameState(cards = cards, pairCount = 1)

        // Try to flip card with ID 1 (which is at index 0)
        val (newState, _) = MatchEvaluator.flipCard(state, 1)

        // Verify card with ID 1 is face up
        val flippedCard = newState.cards.first { it.id == 1 }
        assertTrue(flippedCard.isFaceUp, "Card with ID 1 should be face up")

        // Verify it's still at index 0
        assertEquals(1, newState.cards[0].id)
        assertTrue(newState.cards[0].isFaceUp)
    }

    @Test
    fun flipCard_shouldFindCard_whenIdMatchesIndex() {
        val card1 = CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 1, suit = Suit.Spades, rank = Rank.King)

        val cards = persistentListOf(card1, card2)

        val state = MemoryGameState(cards = cards, pairCount = 1)

        // Try to flip card with ID 1 (which is at index 1)
        val (newState, _) = MatchEvaluator.flipCard(state, 1)

        // Verify card with ID 1 is face up
        val flippedCard = newState.cards.first { it.id == 1 }
        assertTrue(flippedCard.isFaceUp, "Card with ID 1 should be face up")
    }
}
