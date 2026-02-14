package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.ScoringCalculator
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig

/**
 * Domain service responsible for all score-related calculations.
 * Wraps [ScoringCalculator] as a stateless service.
 */
object ScoreKeeper {
    /**
     * Calculates the score for a single match, accounting for combos and Double Down.
     */
    fun calculateMatchScore(
        currentScore: Int,
        isDoubleDownActive: Boolean,
        matchBasePoints: Int,
        matchComboBonus: Int,
        isWon: Boolean,
    ): ScoringCalculator.MatchScoreResult =
        ScoringCalculator.calculateMatchScore(
            currentScore = currentScore,
            isDoubleDownActive = isDoubleDownActive,
            matchBasePoints = matchBasePoints,
            matchComboBonus = matchComboBonus,
            isWon = isWon,
        )

    /**
     * Applies final bonuses (time and move efficiency) when the game is won.
     */
    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = ScoringCalculator.applyFinalBonuses(state, elapsedTimeSeconds)

    /**
     * Determines which game event to fire based on the match result.
     */
    fun determineSuccessEvent(
        isWon: Boolean,
        comboMultiplier: Int,
        config: ScoringConfig,
    ): GameDomainEvent =
        ScoringCalculator.determineSuccessEvent(
            isWon = isWon,
            comboMultiplier = comboMultiplier,
            config = config,
        )
}
