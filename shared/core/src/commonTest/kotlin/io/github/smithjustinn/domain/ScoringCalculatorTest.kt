package io.github.smithjustinn.domain

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
                score = 100,
                pairCount = 8,
                moves = 20,
                mode = GameMode.DAILY_CHALLENGE,
                config =
                    ScoringConfig(
                        timeBonusPerPair = 10,
                        timePenaltyPerSecond = 1,
                        moveBonusMultiplier = 1000,
                    ),
            )

        val resultState = ScoringCalculator.applyFinalBonuses(initialState, elapsedTimeSeconds = 30)

        // Expected Score:
        // Base: 100
        // Time Bonus: (8 * 10 - 30 * 1) = 50
        // Move Bonus: (8 / 20 * 1000) = 400
        // Total Score: 100 + 50 + 400 = 550
        // Earned Currency: 550 + 500 = 1050

        assertEquals(550, resultState.score)
        assertEquals(1050, resultState.scoreBreakdown.earnedCurrency)
        assertEquals(500, resultState.scoreBreakdown.dailyChallengeBonus)
    }

    @Test
    fun `applyFinalBonuses does not add daily challenge bonus in other modes`() {
        val initialState =
            MemoryGameState(
                isGameWon = true,
                score = 100,
                pairCount = 8,
                moves = 20,
                mode = GameMode.TIME_ATTACK,
                config =
                    ScoringConfig(
                        timeBonusPerPair = 10,
                        timePenaltyPerSecond = 1,
                        moveBonusMultiplier = 1000,
                    ),
            )

        val resultState = ScoringCalculator.applyFinalBonuses(initialState, elapsedTimeSeconds = 30)

        // Time Attack uses different time bonus logic: elapsedTimeSeconds * TIME_ATTACK_BONUS_MULTIPLIER (10)
        // Time Bonus: 30 * 10 = 300
        // Move Bonus: 400
        // Total Score: 100 + 300 + 400 = 800
        // Earned Currency: 800

        assertEquals(800, resultState.score)
        assertEquals(800, resultState.scoreBreakdown.earnedCurrency)
        assertEquals(0, resultState.scoreBreakdown.dailyChallengeBonus)
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
