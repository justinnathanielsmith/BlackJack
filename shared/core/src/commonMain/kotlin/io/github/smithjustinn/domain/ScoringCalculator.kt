package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MatchScoreResult
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.domain.models.ScoringConfig

/**
 * Pure functions for calculating match scores and bonuses.
 */
object ScoringCalculator {
    private const val TIME_ATTACK_BONUS_MULTIPLIER = 10
    private const val DAILY_CHALLENGE_CURRENCY_BONUS = 500
    private const val CURRENCY_DIVISOR = 100
    private const val DOUBLE_DOWN_MULTIPLIER = 2

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
        val multiplier = if (isDoubleDownActive) DOUBLE_DOWN_MULTIPLIER else 1

        val (finalScore, ddBonus) =
            if (isWon && isDoubleDownActive) {
                val totalBeforeMultiplier = currentScore + matchPoints
                totalBeforeMultiplier * multiplier to totalBeforeMultiplier
            } else {
                val matchTotal = matchPoints * multiplier
                currentScore + matchTotal to if (isDoubleDownActive) matchPoints else 0
            }

        return MatchScoreResult(
            finalScore = finalScore,
            ddBonus = ddBonus,
        )
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
        val timeBonus = calculateTimeBonus(state, elapsedTimeSeconds, config)

        // Move Efficiency Bonus (dominant factor)
        val moveEfficiency = state.pairCount.toDouble() / state.moves.toDouble()
        val moveBonus = (moveEfficiency * config.moveBonusMultiplier).toInt()

        val totalScore = state.score + timeBonus + moveBonus

        val earnedCurrency = calculateEarnedCurrency(state, totalScore)
        val dailyChallengeBonus = if (state.mode == GameMode.DAILY_CHALLENGE) DAILY_CHALLENGE_CURRENCY_BONUS else 0

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

    private fun calculateTimeBonus(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
        config: ScoringConfig,
    ): Int =
        if (state.mode == GameMode.TIME_ATTACK) {
            (elapsedTimeSeconds * TIME_ATTACK_BONUS_MULTIPLIER).toInt()
        } else {
            (state.pairCount * config.timeBonusPerPair - elapsedTimeSeconds * config.timePenaltyPerSecond)
                .coerceAtLeast(0)
                .toInt()
        }

    private fun calculateEarnedCurrency(
        state: MemoryGameState,
        totalScore: Int,
    ): Int =
        if (state.mode == GameMode.DAILY_CHALLENGE) {
            totalScore / CURRENCY_DIVISOR + DAILY_CHALLENGE_CURRENCY_BONUS
        } else {
            (totalScore / CURRENCY_DIVISOR * state.difficulty.payoutMultiplier).toInt()
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
