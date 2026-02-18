package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import kotlinx.collections.immutable.persistentListOf
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class MutatorEngineTest {

    private fun createCard(id: Int, isMatched: Boolean = false): CardState {
        return CardState(
            id = id,
            suit = Suit.Hearts, // Arbitrary suit
            rank = Rank.Ace, // Arbitrary rank
            isMatched = isMatched
        )
    }

    private fun createGameState(
        moves: Int,
        activeMutators: Set<DailyChallengeMutator>,
        cards: List<CardState>
    ): MemoryGameState {
        return MemoryGameState(
            cards = persistentListOf(*cards.toTypedArray()),
            moves = moves,
            activeMutators = activeMutators,
            pairCount = cards.size / 2
        )
    }

    @Test
    fun applyMutators_shouldDoNothing_whenMirageNotActive() {
        val cards = listOf(
            createCard(1),
            createCard(2),
            createCard(3),
            createCard(4)
        )
        val initialState = createGameState(
            moves = 5,
            activeMutators = emptySet(),
            cards = cards
        )

        val newState = MutatorEngine.applyMutators(initialState)

        assertEquals(initialState, newState)
    }

    @Test
    fun applyMutators_shouldDoNothing_whenMovesNotMultipleOfInterval() {
        val cards = listOf(
            createCard(1),
            createCard(2),
            createCard(3),
            createCard(4)
        )
        val initialState = createGameState(
            moves = 4, // MIRAGE interval is 5
            activeMutators = setOf(DailyChallengeMutator.MIRAGE),
            cards = cards
        )

        val newState = MutatorEngine.applyMutators(initialState)

        assertEquals(initialState, newState)
    }

    @Test
    fun applyMutators_shouldDoNothing_whenMovesIsZero() {
        val cards = listOf(
            createCard(1),
            createCard(2),
            createCard(3),
            createCard(4)
        )
        val initialState = createGameState(
            moves = 0,
            activeMutators = setOf(DailyChallengeMutator.MIRAGE),
            cards = cards
        )

        val newState = MutatorEngine.applyMutators(initialState)

        assertEquals(initialState, newState)
    }

    @Test
    fun applyMutators_shouldDoNothing_whenInsufficientUnmatchedCards() {
        val cards = listOf(
            createCard(1, isMatched = true),
            createCard(2, isMatched = true),
            createCard(3, isMatched = true), // Only 1 unmatched card
            createCard(4)
        )
        val initialState = createGameState(
            moves = 5,
            activeMutators = setOf(DailyChallengeMutator.MIRAGE),
            cards = cards
        )

        val newState = MutatorEngine.applyMutators(initialState)

        assertEquals(initialState, newState)
    }

    @Test
    fun applyMutators_shouldSwapCards_whenMirageActiveAndConditionsMet() {
        val card1 = createCard(1)
        val card2 = createCard(2)
        val card3 = createCard(3)
        val card4 = createCard(4)

        val cards = listOf(card1, card2, card3, card4)

        val initialState = createGameState(
            moves = 5,
            activeMutators = setOf(DailyChallengeMutator.MIRAGE),
            cards = cards
        )

        val newState = MutatorEngine.applyMutators(initialState, Random(12345))

        assertNotEquals(initialState, newState)

        // Find indices that changed
        val changedIndices = cards.indices.filter { i ->
            initialState.cards[i] != newState.cards[i]
        }

        // Assert exactly 2 cards changed positions
        assertEquals(2, changedIndices.size, "Exactly two cards should be swapped")

        val idx1 = changedIndices[0]
        val idx2 = changedIndices[1]

        // Verify the swap logic
        assertEquals(initialState.cards[idx1], newState.cards[idx2], "Card at idx1 should be at idx2")
        assertEquals(initialState.cards[idx2], newState.cards[idx1], "Card at idx2 should be at idx1")

        // Verify unmatched status of swapped cards (though all are unmatched in this setup)
        assertTrue(!initialState.cards[idx1].isMatched, "Swapped card 1 must be unmatched")
        assertTrue(!initialState.cards[idx2].isMatched, "Swapped card 2 must be unmatched")
    }

    @Test
    fun applyMutators_shouldNotSwapMatchedCards() {
        val card1 = createCard(1, isMatched = true) // Matched at index 0
        val card2 = createCard(2, isMatched = true) // Matched at index 1
        val card3 = createCard(3) // Unmatched at index 2
        val card4 = createCard(4) // Unmatched at index 3

        val cards = listOf(card1, card2, card3, card4)

        val initialState = createGameState(
            moves = 5,
            activeMutators = setOf(DailyChallengeMutator.MIRAGE),
            cards = cards
        )

        // Use a random instance, exact seed doesn't matter for property check
        val newState = MutatorEngine.applyMutators(initialState, Random(1))

        assertNotEquals(initialState, newState)

        // Find indices that changed
        val changedIndices = cards.indices.filter { i ->
            initialState.cards[i] != newState.cards[i]
        }

        // Assert exactly 2 cards changed positions
        assertEquals(2, changedIndices.size, "Exactly two cards should be swapped")

        // Assert matched cards did NOT change
        assertTrue(!changedIndices.contains(0), "Matched card at index 0 should not move")
        assertTrue(!changedIndices.contains(1), "Matched card at index 1 should not move")

        // Assert unmatched cards did change
        assertTrue(changedIndices.contains(2), "Unmatched card at index 2 should move")
        assertTrue(changedIndices.contains(3), "Unmatched card at index 3 should move")
    }
}
