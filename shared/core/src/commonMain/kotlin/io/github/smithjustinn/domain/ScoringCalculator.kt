package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.domain.models.ScoringConfig

/**
 * Pure functions for calculating match scores and bonuses.
 */
object ScoringCalculator {
    private const val TIME_ATTACK_BONUS_MULTIPLIER = 10
    private const val DAILY_CHALLENGE_CURRENCY_BONUS = 500

    data class MatchScoreResult(
        val finalScore: Int,
        val ddBonus: Int,
    )

    /**
     * Calculates the score for a single match, accounting for combos and Double Down.
     */
    fun calculateMatchScore(
        currentScore: Int,
        isDoubleDownActive: Boolean,
        matchBasePoints: Int,
        matchComboBonus: Int,
        isWon: Boolean,
    ): MatchScoreResult {
        val matchPoints = matchBasePoints + matchComboBonus

        return if (isWon && isDoubleDownActive) {
            val totalWithoutBonus = currentScore + matchPoints
            val finalScore = totalWithoutBonus * 2
            MatchScoreResult(
                finalScore = finalScore,
                ddBonus = finalScore - totalWithoutBonus,
            )
        } else {
            val multiplier = if (isDoubleDownActive) 2 else 1
            val matchTotal = matchPoints * multiplier
            MatchScoreResult(
                finalScore = currentScore + matchTotal,
                ddBonus = if (isDoubleDownActive) matchTotal / 2 else 0,
            )
        }
    }

    /**
     * Calculates final bonuses (Time and Move Efficiency) when the game is won.
     */
    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState {
        if (!state.isGameWon) return state

        val config = state.config

        // Time Bonus
        val timeBonus =
            if (state.mode == GameMode.TIME_ATTACK) {
                // In Time Attack, remaining time is the bonus
                (elapsedTimeSeconds * TIME_ATTACK_BONUS_MULTIPLIER).toInt()
            } else {
                (state.pairCount * config.timeBonusPerPair - (elapsedTimeSeconds * config.timePenaltyPerSecond))
                    .coerceAtLeast(0)
                    .toInt()
            }

        // Move Efficiency Bonus (dominant factor)
        val moveEfficiency = state.pairCount.toDouble() / state.moves.toDouble()
        val moveBonus = (moveEfficiency * config.moveBonusMultiplier).toInt()

        val totalScore = state.score + timeBonus + moveBonus

        val earnedCurrency =
            if (state.mode == GameMode.DAILY_CHALLENGE) {
                totalScore + DAILY_CHALLENGE_CURRENCY_BONUS
            } else {
                totalScore
            }

        val dailyChallengeBonus =
            if (state.mode == GameMode.DAILY_CHALLENGE) {
                DAILY_CHALLENGE_CURRENCY_BONUS
            } else {
                0
            }

        return state.copy(
            score = totalScore,
            scoreBreakdown =
                ScoreBreakdown(
                    basePoints = state.totalBasePoints,
                    comboBonus = state.totalComboBonus,
                    doubleDownBonus = state.totalDoubleDownBonus,
                    timeBonus = timeBonus,
                    moveBonus = moveBonus,
                    dailyChallengeBonus = dailyChallengeBonus,
                    totalScore = totalScore,
                    earnedCurrency = earnedCurrency,
                ),
        )
    }

    /**
     * Determines which game event to fire based on the match result.
     */
    fun determineSuccessEvent(
        isWon: Boolean,
        comboMultiplier: Int,
        config: ScoringConfig,
    ): GameDomainEvent =
        when {
            isWon -> GameDomainEvent.GameWon
            comboMultiplier > config.theNutsThreshold -> GameDomainEvent.TheNutsAchieved
            else -> GameDomainEvent.MatchSuccess
        }
}
