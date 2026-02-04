package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringCalculatorTest {
    @Test
    fun `applyFinalBonuses adds daily challenge bonus in Daily Challenge mode`() {
        val initialState =
            MemoryGameState(
                isGameWon = true,
                score = 10000, // Normalized for scaling
                pairCount = 8,
                moves = 20,
                mode = GameMode.DAILY_CHALLENGE,
                difficulty = DifficultyType.CASUAL,
                config =
                    ScoringConfig(
                        timeBonusPerPair = 10,
                        timePenaltyPerSecond = 1,
                        moveBonusMultiplier = 1000,
                    ),
            )

        val resultState = ScoringCalculator.applyFinalBonuses(initialState, elapsedTimeSeconds = 30)

        // Expected Score:
        // Base: 10000
        // Time Bonus: (8 * 10 - 30 * 1) = 50
        // Move Bonus: (8 / 20 * 1000) = 400
        // Total Score: 10000 + 50 + 400 = 10450
        // Earned Currency: (10450 / 100) + 500 = 104 + 500 = 604

        assertEquals(10450, resultState.score)
        assertEquals(604, resultState.scoreBreakdown.earnedCurrency)
        assertEquals(500, resultState.scoreBreakdown.dailyChallengeBonus)
    }

    @Test
    fun `applyFinalBonuses applies difficultly multipliers correctly`() {
        val score = 10000

        val touristState =
            MemoryGameState(
                isGameWon = true,
                score = score,
                difficulty = DifficultyType.TOURIST,
                mode = GameMode.TIME_ATTACK,
                moves = 8,
            )
        val casualState =
            MemoryGameState(
                isGameWon = true,
                score = score,
                difficulty = DifficultyType.CASUAL,
                mode = GameMode.TIME_ATTACK,
                moves = 8,
            )
        val masterState =
            MemoryGameState(
                isGameWon = true,
                score = score,
                difficulty = DifficultyType.MASTER,
                mode = GameMode.TIME_ATTACK,
                moves = 8,
            )
        val sharkState =
            MemoryGameState(
                isGameWon = true,
                score = score,
                difficulty = DifficultyType.SHARK,
                mode = GameMode.TIME_ATTACK,
                moves = 8,
            )

        val touristResult = ScoringCalculator.applyFinalBonuses(touristState, 0)
        val casualResult = ScoringCalculator.applyFinalBonuses(casualState, 0)
        val masterResult = ScoringCalculator.applyFinalBonuses(masterState, 0)
        val sharkResult = ScoringCalculator.applyFinalBonuses(sharkState, 0)

        // Time Attack uses simplified time bonus: 0 * 10 = 0
        // Move Bonus: 8 / 8 * 10000 = 10000
        // Total Score: 10000 + 10000 = 20000

        // Tourist: (20000 / 100) * 0.25 = 200 * 0.25 = 50
        // Casual: (20000 / 100) * 1.0 = 200 * 1.0 = 200
        // Master: (20000 / 100) * 2.5 = 200 * 2.5 = 500
        // Shark: (20000 / 100) * 5.0 = 200 * 5.0 = 1000

        assertEquals(50, touristResult.scoreBreakdown.earnedCurrency)
        assertEquals(200, casualResult.scoreBreakdown.earnedCurrency)
        assertEquals(500, masterResult.scoreBreakdown.earnedCurrency)
        assertEquals(1000, sharkResult.scoreBreakdown.earnedCurrency)
    }

    @Test
    fun `calculateMatchScore handles Double Down correctly`() {
        val result =
            ScoringCalculator.calculateMatchScore(
                currentScore = 100,
                isDoubleDownActive = true,
                matchBasePoints = 50,
                matchComboBonus = 10,
                isWon = false,
            )

        // (50 + 10) * 2 = 120
        // 100 + 120 = 220
        assertEquals(220, result.finalScore)
        assertEquals(60, result.ddBonus)
    }
}
