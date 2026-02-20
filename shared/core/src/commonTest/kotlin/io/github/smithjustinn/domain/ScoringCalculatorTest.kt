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
    fun `calculateMatchUpdate handles Double Down on Win`() {
        val config = ScoringConfig(baseMatchPoints = 50, comboBonusPoints = 10)
        val state =
            MemoryGameState(
                score = 100,
                currentPot = 0,
                comboMultiplier = 1,
                isDoubleDownActive = true,
                config = config,
                pairCount = 8,
            )
        // Match points: 50 + 10*1^2 = 60.
        // Pot: 0 + 60 = 60.
        // ScoreWithPot: 100 + 60 = 160.
        // Win + DD: 160 * 2 = 320.
        // DD Bonus: 160.

        val update = ScoringCalculator.calculateMatchUpdate(state, isWon = true, matchesFound = 8)

        assertEquals(320, update.scoreResult.finalScore)
        assertEquals(160, update.scoreResult.ddBonus)
        assertEquals(60L, update.potentialPot) // Pot for this match
    }

    @Test
    fun `calculateMatchUpdate handles Milestone without Double Down`() {
        val config =
            ScoringConfig(
                baseMatchPoints = 50,
                comboBonusPoints = 10,
                matchMilestoneInterval = 4,
            )
        val state =
            MemoryGameState(
                score = 100,
                currentPot = 200,
                comboMultiplier = 1,
                isDoubleDownActive = true,
                config = config,
                pairCount = 8,
            )
        // Match points: 60.
        // Pot: 200 + 60 = 260.
        // Milestone (4 matches).
        // ScoreWithPot: 100 + 260 = 360.
        // Not Win. No DD multiplier.
        // Final Score: 360.

        val update = ScoringCalculator.calculateMatchUpdate(state, isWon = false, matchesFound = 4)

        assertEquals(360, update.scoreResult.finalScore)
        assertEquals(0, update.scoreResult.ddBonus) // No bonus on milestone
    }
}
