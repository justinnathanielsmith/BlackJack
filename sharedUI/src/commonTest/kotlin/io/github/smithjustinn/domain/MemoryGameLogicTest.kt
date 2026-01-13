package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MemoryGameLogicTest {

    @Test
    fun `createInitialState should create correct number of cards`() {
        val pairCount = 4
        val state = MemoryGameLogic.createInitialState(pairCount)

        assertEquals(pairCount * 2, state.cards.size)
        assertEquals(pairCount, state.pairCount)
        
        // Verify we have pairs
        val groups = state.cards.groupBy { it.suit to it.rank }
        assertEquals(pairCount, groups.size)
        groups.forEach { (_, cards) ->
            assertEquals(2, cards.size)
        }
    }

    @Test
    fun `flipCard should flip a face-down card`() {
        val state = MemoryGameLogic.createInitialState(4)
        val cardId = state.cards[0].id
        
        val (newState, event) = MemoryGameLogic.flipCard(state, cardId)
        
        assertTrue(newState.cards.first { it.id == cardId }.isFaceUp)
        assertNull(event)
    }

    @Test
    fun `flipCard should do nothing if card is already face-up`() {
        val initialState = MemoryGameLogic.createInitialState(4)
        val cardId = initialState.cards[0].id
        val (stateAfterFirstFlip, _) = MemoryGameLogic.flipCard(initialState, cardId)
        
        val (stateAfterSecondFlip, event) = MemoryGameLogic.flipCard(stateAfterFirstFlip, cardId)
        
        assertEquals(stateAfterFirstFlip, stateAfterSecondFlip)
        assertNull(event)
    }

    @Test
    fun `flipCard should detect match when two identical cards are flipped`() {
        val pairCount = 2
        val initialState = MemoryGameLogic.createInitialState(pairCount)
        
        // Find a pair
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards.first { it.id != firstCard.id && it.suit == firstCard.suit && it.rank == firstCard.rank }
        
        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, event) = MemoryGameLogic.flipCard(state1, secondCard.id)
        
        assertEquals(GameDomainEvent.MatchSuccess, event)
        assertTrue(state2.cards.first { it.id == firstCard.id }.isMatched)
        assertTrue(state2.cards.first { it.id == secondCard.id }.isMatched)
        assertEquals(1, state2.moves)
        assertEquals(state2.config.baseMatchPoints, state2.score)
    }

    @Test
    fun `flipCard should detect failure when two different cards are flipped`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)
        
        // Find two different cards
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards.first { it.suit != firstCard.suit || it.rank != firstCard.rank }
        
        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, event) = MemoryGameLogic.flipCard(state1, secondCard.id)
        
        assertEquals(GameDomainEvent.MatchFailure, event)
        assertTrue(state2.cards.first { it.id == firstCard.id }.isError)
        assertTrue(state2.cards.first { it.id == secondCard.id }.isError)
        assertEquals(1, state2.moves)
        assertEquals(0, state2.score)
    }

    @Test
    fun `resetErrorCards should flip back error cards`() {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards.first { it.suit != firstCard.suit || it.rank != firstCard.rank }
        
        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, _) = MemoryGameLogic.flipCard(state1, secondCard.id)
        
        val state3 = MemoryGameLogic.resetErrorCards(state2)
        
        assertFalse(state3.cards.any { it.isError })
        assertFalse(state3.cards.any { it.isFaceUp })
    }

    @Test
    fun `game should be won when all pairs are matched`() {
        val pairCount = 1
        val initialState = MemoryGameLogic.createInitialState(pairCount)
        
        val firstCard = initialState.cards[0]
        val secondCard = initialState.cards[1]
        
        val (state1, _) = MemoryGameLogic.flipCard(initialState, firstCard.id)
        val (state2, event) = MemoryGameLogic.flipCard(state1, secondCard.id)
        
        assertEquals(GameDomainEvent.GameWon, event)
        assertTrue(state2.isGameWon)
    }

    @Test
    fun `applyFinalBonuses should calculate score correctly`() {
        val pairCount = 2
        var state = MemoryGameLogic.createInitialState(pairCount)
        
        // Manually set the state to win with some stats
        state = state.copy(
            isGameWon = true,
            moves = 2, // Perfect efficiency for 2 pairs
            score = 40 // 2 matches * 20 points
        )
        
        val finalState = MemoryGameLogic.applyFinalBonuses(state, 10) // 10 seconds
        
        assertTrue(finalState.score > state.score)
        assertNotNull(finalState.scoreBreakdown)
        assertEquals(40, finalState.scoreBreakdown.matchPoints)
        assertTrue(finalState.scoreBreakdown.timeBonus >= 0)
        assertTrue(finalState.scoreBreakdown.moveBonus > 0)
        assertEquals(finalState.score, finalState.scoreBreakdown.totalScore)
    }
}
