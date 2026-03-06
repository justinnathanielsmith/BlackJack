package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreKeeperTest {
    @Test
    fun `calculateMatchUpdate delegates to ScoringCalculator`() {
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

        val update = ScoreKeeper.calculateMatchUpdate(state, isWon = true, matchesFound = 8)

        // Verifying it delegates to ScoringCalculator properly
        assertEquals(320, update.scoreResult.finalScore)
        assertEquals(160, update.scoreResult.ddBonus)
        assertEquals(60L, update.potentialPot)
        assertEquals(50, update.matchBasePoints)
        assertEquals(10, update.matchComboBonus)
        assertEquals(true, update.isMilestone)
    }

    @Test
    fun `applyFinalBonuses delegates to ScoringCalculator`() {
        val initialState =
            MemoryGameState(
                isGameWon = true,
                score = 10000,
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

        val resultState = ScoreKeeper.applyFinalBonuses(initialState, elapsedTimeSeconds = 30)

        assertEquals(10450, resultState.score)
        assertEquals(604, resultState.scoreBreakdown?.earnedCurrency)
        assertEquals(500, resultState.scoreBreakdown?.dailyChallengeBonus)
    }

    @Test
    fun `determineSuccessEvent returns GameWon when isWon is true`() {
        val config = ScoringConfig()
        val event =
            ScoreKeeper.determineSuccessEvent(
                isWon = true,
                comboMultiplier = 1,
                config = config,
            )

        assertEquals(GameDomainEvent.GameWon, event)
    }

    @Test
    fun `determineSuccessEvent returns TheNutsAchieved when comboMultiplier is greater than threshold`() {
        val config = ScoringConfig(theNutsThreshold = 3)
        val event =
            ScoreKeeper.determineSuccessEvent(
                isWon = false,
                comboMultiplier = 4,
                config = config,
            )

        assertEquals(GameDomainEvent.TheNutsAchieved, event)
    }

    @Test
    fun `determineSuccessEvent returns MatchSuccess when not won and below threshold`() {
        val config = ScoringConfig(theNutsThreshold = 3)
        val event =
            ScoreKeeper.determineSuccessEvent(
                isWon = false,
                comboMultiplier = 2,
                config = config,
            )

        assertEquals(GameDomainEvent.MatchSuccess, event)
    }
}
