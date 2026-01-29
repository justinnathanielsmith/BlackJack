package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlinx.collections.immutable.toImmutableList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MemoryGameLogicTest {
    @Test
    fun `createInitialState creates correct number of pairs`() {
        val pairs = 6
        val state = MemoryGameLogic.createInitialState(pairCount = pairs)
        assertEquals(pairs * 2, state.cards.size)
        // Verify we have pairs
        val grouped = state.cards.groupBy { it.suit to it.rank }
        assertEquals(pairs, grouped.size)
        grouped.values.forEach {
            assertEquals(2, it.size)
        }
    }

    @Test
    fun `flipCard updates state correctly for first card`() {
        val state = MemoryGameLogic.createInitialState(pairCount = 2)
        val cardToFlip = state.cards[0]
        val (newState, event) = MemoryGameLogic.flipCard(state, cardToFlip.id)

        assertTrue(newState.cards[0].isFaceUp)
        assertEquals(GameDomainEvent.CardFlipped, event)
    }

    @Test
    fun `flipCard ignores already matched or face up cards`() {
        var state = MemoryGameLogic.createInitialState(pairCount = 2)
        // Flip first card
        state = MemoryGameLogic.flipCard(state, state.cards[0].id).first

        // Try flip again
        val (sameState, event) = MemoryGameLogic.flipCard(state, state.cards[0].id)
        assertEquals(state, sameState)
        assertEquals(null, event)
    }

    @Test
    fun `flipCard handles match success`() {
        // Create state with known setup is hard due to shuffling,
        // so we have to find a matching pair in the state
        val state = MemoryGameLogic.createInitialState(pairCount = 2)
        val firstCard = state.cards[0]
        val matchCard = state.cards.drop(1).first { it.suit == firstCard.suit && it.rank == firstCard.rank }

        // Flip first
        val (currentState, _) = MemoryGameLogic.flipCard(state, firstCard.id)

        // Flip match
        val (finalState, event) = MemoryGameLogic.flipCard(currentState, matchCard.id)

        assertTrue(finalState.cards.find { it.id == firstCard.id }!!.isMatched)
        assertTrue(finalState.cards.find { it.id == matchCard.id }!!.isMatched)
        assertTrue(finalState.score > 0)
        assertTrue(finalState.comboMultiplier > 0)

        // Check event type (could be GameWon if only 1 pair, so let's check basic success first)
        // With 2 pairs, matching one shouldn't win immediately
        assertTrue(event is GameDomainEvent.MatchSuccess || event is GameDomainEvent.GameWon)
    }

    @Test
    fun `flipCard handles match failure`() {
        val state = MemoryGameLogic.createInitialState(pairCount = 6)
        val firstCard = state.cards[0]
        // Find a non-matching card
        val nonMatchCard = state.cards.drop(1).first { it.suit != firstCard.suit || it.rank != firstCard.rank }

        // Flip first
        val (currentState, _) = MemoryGameLogic.flipCard(state, firstCard.id)

        // Flip non-match
        val (finalState, event) = MemoryGameLogic.flipCard(currentState, nonMatchCard.id)

        assertFalse(finalState.cards.find { it.id == firstCard.id }!!.isMatched)
        assertTrue(finalState.cards.find { it.id == firstCard.id }!!.isError)
        assertEquals(0, finalState.comboMultiplier)
        assertEquals(GameDomainEvent.MatchFailure, event)
    }

    @Test
    fun `activateDoubleDown enables double down when eligible`() {
        val config = ScoringConfig(heatModeThreshold = 3)
        // Need to simulate a state with high combo
        var state = MemoryGameLogic.createInitialState(pairCount = 6, config = config)
        state = state.copy(comboMultiplier = 3)

        val ddState = MemoryGameLogic.activateDoubleDown(state)
        assertTrue(ddState.isDoubleDownActive)
    }

    @Test
    fun `double down failure causes bust`() {
        val pairs = 6
        var state = MemoryGameLogic.createInitialState(pairCount = pairs)
        state = state.copy(isDoubleDownActive = true, score = 1000)

        val firstCard = state.cards[0]
        val nonMatchCard = state.cards.drop(1).first { it.suit != firstCard.suit || it.rank != firstCard.rank }

        val (currentState, _) = MemoryGameLogic.flipCard(state, firstCard.id)
        val (finalState, event) = MemoryGameLogic.flipCard(currentState, nonMatchCard.id)

        assertTrue(finalState.isBusted)
        assertEquals(0, finalState.score)
        assertEquals(GameDomainEvent.GameOver, event)
    }

    @Test
    fun `applyFinalBonuses calculates correctly`() {
        val config =
            ScoringConfig(
                baseMatchPoints = 100,
                comboBonusPoints = 50,
                timeBonusPerPair = 10,
                timePenaltyPerSecond = 1,
                moveBonusMultiplier = 100,
            )
        // Simulate a won state
        var state = MemoryGameLogic.createInitialState(pairCount = 2, config = config)
        state =
            state.copy(
                isGameWon = true,
                score = 500,
                moves = 4,
                pairCount = 2,
            )

        // 2 pairs, 4 moves => efficiency = 0.5. Bonus = 0.5 * 100 = 50
        // Time bonus: (2 * 10) - (5 * 1) = 20 - 5 = 15
        // Expect: 500 + 50 + 15 = 565

        val finalState = MemoryGameLogic.applyFinalBonuses(state, elapsedTimeSeconds = 5)

        assertEquals(565, finalState.score)
        assertNotNull(finalState.scoreBreakdown)
        assertEquals(50, finalState.scoreBreakdown.moveBonus)
        assertEquals(15, finalState.scoreBreakdown.timeBonus)
    }

    @Test
    fun `resetErrorCards clears error state`() {
        var state = MemoryGameLogic.createInitialState(pairCount = 2)
        // Manually set some error cards
        val errorCards =
            state.cards
                .mapIndexed { index, card ->
                    if (index < 2) card.copy(isError = true, isFaceUp = true) else card
                }.toImmutableList() // Convert back to immutable list

        state = state.copy(cards = errorCards)

        val resetState = MemoryGameLogic.resetErrorCards(state)
        assertTrue(resetState.cards.none { it.isError })
        assertTrue(resetState.cards.take(2).none { it.isFaceUp }) // Should flip back down
    }

    @Test
    fun `calculateInitialTime returns correct values`() {
        assertEquals(25L, MemoryGameLogic.calculateInitialTime(6))
        assertEquals(35L, MemoryGameLogic.calculateInitialTime(8))
        assertEquals(45L, MemoryGameLogic.calculateInitialTime(10))
        assertEquals(55L, MemoryGameLogic.calculateInitialTime(12))
        assertEquals(12L, MemoryGameLogic.calculateInitialTime(3)) // Fallback 3 * 4
    }

    @Test
    fun `calculateTimeGain scales with combo`() {
        // Base = 3, Multiplier = 2
        // Combo 1: 3 + (0 * 2) = 3
        assertEquals(3, MemoryGameLogic.calculateTimeGain(1))
        // Combo 2: 3 + (1 * 2) = 5
        assertEquals(5, MemoryGameLogic.calculateTimeGain(2))
        // Combo 5: 3 + (4 * 2) = 11
        assertEquals(11, MemoryGameLogic.calculateTimeGain(5))
    }
}
