package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class ScoringCalculatorSecurityTest {
    @Test
    fun `applyFinalBonuses handles zero moves gracefully`() {
        val initialState =
            MemoryGameState(
                isGameWon = true,
                score = 100,
                pairCount = 8,
                moves = 0, // Vulnerability Trigger
                mode = GameMode.TIME_ATTACK,
                difficulty = DifficultyType.CASUAL,
                config =
                    ScoringConfig(
                        timeBonusPerPair = 10,
                        timePenaltyPerSecond = 1,
                        moveBonusMultiplier = 1000,
                    ),
            )

        // This should not crash or produce massive overflow
        val resultState = ScoringCalculator.applyFinalBonuses(initialState, elapsedTimeSeconds = 30)

        // Check for sanity
        // If overflow happens, score might be negative
        assertTrue(resultState.score >= 100, "Score should not decrease due to overflow. Actual: ${resultState.score}")
        assertTrue(
            resultState.score < 2_000_000_000,
            "Score should not be unreasonably high due to infinity. Actual: ${resultState.score}",
        )
    }

    @Test
    fun `applyFinalBonuses throws on negative elapsed time`() {
        val state =
            MemoryGameState(
                isGameWon = true,
                score = 100,
                pairCount = 8,
                mode = GameMode.TIME_ATTACK,
                difficulty = DifficultyType.CASUAL,
                config = ScoringConfig(),
            )

        assertFailsWith<IllegalArgumentException> {
            ScoringCalculator.applyFinalBonuses(state, elapsedTimeSeconds = -100)
        }
    }

    @Test
    fun `calculateMatchUpdate throws on negative matches found`() {
        val state = MemoryGameState()

        assertFailsWith<IllegalArgumentException> {
            ScoringCalculator.calculateMatchUpdate(state, isWon = false, matchesFound = -1)
        }
    }
}
