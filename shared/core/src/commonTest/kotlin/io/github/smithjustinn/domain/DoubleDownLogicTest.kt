package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.domain.services.MatchEvaluator
import kotlinx.collections.immutable.toPersistentList
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DoubleDownLogicTest {
    private val defaultConfig = ScoringConfig()

    @Test
    fun `activateDoubleDown should FAIL if unmatched pairs less than 3`() {
        // Arrange: Create state with only 2 pairs
        val state =
            GameFactory
                .createInitialState(pairCount = 2, config = defaultConfig)
                .copy(comboMultiplier = defaultConfig.heatModeThreshold) // Heat Mode ready

        // Act
        val result = MatchEvaluator.activateDoubleDown(state)

        // Assert
        assertFalse(result.isDoubleDownActive, "Double Down should NOT activate with < 3 pairs")
    }

    @Test
    fun `activateDoubleDown should SUCCEED if unmatched pairs greater or equal to 3`() {
        // Arrange: Create state with 3 pairs
        val state =
            GameFactory
                .createInitialState(pairCount = 3, config = defaultConfig)
                .copy(comboMultiplier = defaultConfig.heatModeThreshold) // Heat Mode ready

        // Act
        val result = MatchEvaluator.activateDoubleDown(state)

        // Assert
        assertTrue(result.isDoubleDownActive, "Double Down SHOULD activate with >= 3 pairs")
    }

    @Test
    fun `handleMatchFailure while Double Down active should causes Game Over and Zero Score`() {
        // Arrange
        var state =
            GameFactory
                .createInitialState(pairCount = 4, config = defaultConfig)
                .copy(comboMultiplier = defaultConfig.heatModeThreshold)

        state = MatchEvaluator.activateDoubleDown(state)
        assertTrue(state.isDoubleDownActive)

        // Find two mismatching cards
        val card1 = state.cards[0]
        val card2 = state.cards.first { it.rank != card1.rank || it.suit != card1.suit }

        // Start flip
        val (flippedState, _) = MatchEvaluator.flipCard(state, card1.id)

        // Act: Flip mismatch
        val (finalState, event) = MatchEvaluator.flipCard(flippedState, card2.id)

        // Assert
        assertEquals(GameDomainEvent.GameOver, event)
        assertEquals(0, finalState.score, "Score should be 0 on Double Down failure")
        assertTrue(finalState.isGameOver, "Game should be over")
        assertTrue(finalState.isBusted, "Game should be marked as Busted")
    }

    @Test
    fun `handleMatchSuccess on Final Pair while Double Down active should Double The Score`() {
        // Arrange: 3 pairs, match 2 of them normally to build score
        // Construct a state where we are "About to Win" with Double Down active
        // Let's assume we have 1 pair left and D-Down is active (activated previously when pairs >= 3)
        // Score: 1000
        val card1 = CardState(0, Suit.Hearts, Rank.Ace)
        val card2 = CardState(1, Suit.Hearts, Rank.Ace)
        val cards = listOf(card1, card2).toPersistentList()

        val startScore = 1000
        val basePoints = 100
        val comboBonus = 50
        // Logic: (1000 + (100 + (combofactor * 50))) * 2
        // Let's set combo to 0 for simplicity or mimic logic

        val state =
            MemoryGameState(
                cards = cards,
                pairCount = 1,
                config = defaultConfig,
                mode = GameMode.TIME_ATTACK,
                score = startScore,
                isDoubleDownActive = true,
                comboMultiplier = 3,
            )

        // Act: Match the final pair
        val (flippedState, _) = MatchEvaluator.flipCard(state, card1.id)
        val (finalState, _) = MatchEvaluator.flipCard(flippedState, card2.id)

        // Assert
        assertTrue(finalState.isGameWon)

        // Calculate Expected
        val comboFactor = 3 * 3 // 9
        val matchPoints = basePoints + comboFactor * comboBonus // 100 + 450 = 550
        val preDoubleScore = startScore + matchPoints // 1550
        val expectedScore = preDoubleScore * 2 // 3100

        assertEquals(
            expectedScore,
            finalState.score,
            "Score should be doubled on win: Expected $expectedScore but got ${finalState.score}",
        )
        assertFalse(finalState.isDoubleDownActive, "Double Down should deactivate after win")
    }
}
