package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemoryGameLogicTest {

    @Test
    fun testDoubleDownActivation() {
        val config = ScoringConfig(heatModeThreshold = 3)
        var state = MemoryGameLogic.createInitialState(pairCount = 4, config = config)
        
        // Simulating Heat Mode (Combo 3)
        state = state.copy(comboMultiplier = 3)
        
        // Activate Double Down
        val doubleDownState = MemoryGameLogic.activateDoubleDown(state)
        assertTrue(doubleDownState.isDoubleDownActive, "Double Down should be active given adequate combo")
    }

    @Test
    fun testDoubleDownPenalty() {
        val config = ScoringConfig(doubleDownPenalty = 500)
        var state = MemoryGameLogic.createInitialState(pairCount = 4, config = config)
        
        // Setup: Active Double Down, some initial score
        state = state.copy(isDoubleDownActive = true, score = 1000, comboMultiplier = 4)
        
        // Simulate Mismatch (Flip two mismatching cards)
        // Card generation is random, so we need to valid IDs from state
        val card1 = state.cards[0]
        val card2 = state.cards.find { it.suit != card1.suit }!! // Find a mismatch
        
        var (nextState, _) = MemoryGameLogic.flipCard(state, card1.id)
        val (finalState, _) = MemoryGameLogic.flipCard(nextState, card2.id) // Second flip triggers check
        
        // Score should obey: Start(1000) - Penalty(500) = 500
        assertEquals(500, finalState.score, "Score should be reduced by penalty")
        assertFalse(finalState.isDoubleDownActive, "Double Down should be reset after mismatch")
        assertEquals(0, finalState.comboMultiplier, "Combo should reset on mismatch")
    }

    @Test
    fun testDoubleDownBonus() {
        val config = ScoringConfig(baseMatchPoints = 100, comboBonusPoints = 0) // Simplify math
        var state = MemoryGameLogic.createInitialState(pairCount = 4, config = config)
        
        // Setup: Active DD
        state = state.copy(isDoubleDownActive = true, score = 0, comboMultiplier = 1)
        
        val card1 = state.cards[0]
        val card2 = state.cards.find { it.suit == card1.suit && it.rank == card1.rank && it.id != card1.id }!!
        
        var (nextState, _) = MemoryGameLogic.flipCard(state, card1.id)
        val (finalState, _) = MemoryGameLogic.flipCard(nextState, card2.id)
        
        // Score: Base(100) * 2 = 200
        assertEquals(200, finalState.score, "Score should be doubled on match")
        assertFalse(finalState.isDoubleDownActive, "Double Down should be reset after match")
        assertEquals(1, finalState.comboMultiplier, "Combo should reset to 1 after successful Double Down match")
    }
}
