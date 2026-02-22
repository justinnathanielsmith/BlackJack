package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MatchScoreResult
import io.github.smithjustinn.domain.models.MatchScoringUpdate
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
    private const val DOUBLE_DOWN_MULTIPLIER = 2L
    private val MAX_SAFE_INTEGER = Int.MAX_VALUE.toLong()

    /**
     * Calculates the score update for a match, including pot accumulation and milestone checks.
     */
    fun calculateMatchUpdate(
        state: MemoryGameState,
        isWon: Boolean,
        matchesFound: Int,
    ): MatchScoringUpdate {
        val (matchBasePoints, matchComboBonus) = calculateMatchPoints(state)
        val matchTotalPoints = matchBasePoints + matchComboBonus

        val potentialPot = calculatePot(state.currentPot, matchTotalPoints)
        val isMilestone = matchesFound > 0 && matchesFound % state.config.matchMilestoneInterval == 0

        val scoreWithPot =
            if (isMilestone || isWon) {
                state.score + potentialPot
            } else {
                state.score.toLong()
            }

        val (finalScore, ddBonus) = calculateDoubleDown(isWon, state, scoreWithPot)

        return MatchScoringUpdate(
            matchBasePoints = matchBasePoints.toInt(),
            matchComboBonus = matchComboBonus.toInt(),
            potentialPot = potentialPot,
            isMilestone = isMilestone,
            scoreResult =
                MatchScoreResult(
                    finalScore = finalScore.coerceIn(0, MAX_SAFE_INTEGER).toInt(),
                    ddBonus = ddBonus.coerceIn(0, MAX_SAFE_INTEGER).toInt(),
                ),
        )
    }

    private fun calculateMatchPoints(state: MemoryGameState): Pair<Long, Long> {
        val comboFactor = state.comboMultiplier.toLong() * state.comboMultiplier.toLong()
        val matchBasePoints = state.config.baseMatchPoints.toLong()
        val matchComboBonus =
            (comboFactor * state.config.comboBonusPoints)
                .coerceAtMost(MAX_SAFE_INTEGER)
        return matchBasePoints to matchComboBonus
    }

    private fun calculatePot(
        currentPot: Int,
        matchTotalPoints: Long,
    ): Long = (currentPot + matchTotalPoints).coerceAtMost(MAX_SAFE_INTEGER)

    private fun calculateDoubleDown(
        isWon: Boolean,
        state: MemoryGameState,
        scoreWithPot: Long,
    ): Pair<Long, Long> =
        if (isWon && state.isDoubleDownActive) {
            val doubledScore = scoreWithPot * DOUBLE_DOWN_MULTIPLIER
            doubledScore to scoreWithPot
        } else {
            scoreWithPot to 0L
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
        // Ensure moves is at least 1 to avoid division by zero or infinity.
        val effectiveMoves = state.moves.coerceAtLeast(1)
        val moveEfficiency = state.pairCount.toDouble() / effectiveMoves.toDouble()
        val moveBonus = (moveEfficiency * config.moveBonusMultiplier).toInt()

        // Prevent overflow by using Long for intermediate calculation
        val totalScoreLong = state.score.toLong() + timeBonus + moveBonus
        val totalScore = totalScoreLong.coerceIn(0, MAX_SAFE_INTEGER).toInt()

        val earnedCurrency = calculateEarnedCurrency(state, totalScore)
        val dailyChallengeBonus =
            if (state.mode == GameMode.DAILY_CHALLENGE) DAILY_CHALLENGE_CURRENCY_BONUS else 0

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
            (
                state.pairCount * config.timeBonusPerPair -
                    elapsedTimeSeconds * config.timePenaltyPerSecond
            ).coerceAtLeast(0)
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
