package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MatchScoreResult
import io.github.smithjustinn.domain.models.MatchScoringUpdate
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.domain.models.ScoringConfig

private const val TIME_ATTACK_BONUS_MULTIPLIER = 10
private const val DAILY_CHALLENGE_CURRENCY_BONUS = 500
private const val CURRENCY_DIVISOR = 100
private const val DOUBLE_DOWN_MULTIPLIER = 2L
private const val MIN_EFFECTIVE_MOVES = 1
private val MAX_SCORE = Int.MAX_VALUE.toLong()

/**
 * Pure functions for calculating match scores and bonuses.
 */
object ScoringCalculator {

    private data class MatchPoints(
        val base: Long,
        val bonus: Long,
    )

    private data class DoubleDownResult(
        val finalScore: Long,
        val bonus: Long,
    )

    private data class BonusResult(
        val timeBonus: Int,
        val moveBonus: Int,
        val totalScore: Int,
        val earnedCurrency: Int,
        val dailyChallengeBonus: Int,
    )

    /**
     * Calculates the score update for a match, including pot accumulation and milestone checks.
     */
    fun calculateMatchUpdate(
        state: MemoryGameState,
        isWon: Boolean,
        matchesFound: Int,
    ): MatchScoringUpdate {
        val points = calculateMatchPoints(state)
        val matchTotalPoints = points.base + points.bonus

        val potentialPot = calculatePot(state.currentPot, matchTotalPoints)
        val isMilestone = matchesFound > 0 && matchesFound % state.config.matchMilestoneInterval == 0

        val scoreWithPot = calculateScoreWithPot(state, potentialPot, isWon, isMilestone)
        val doubleDownResult = calculateDoubleDown(isWon, state, scoreWithPot)

        return MatchScoringUpdate(
            matchBasePoints = points.base.toInt(),
            matchComboBonus = points.bonus.toInt(),
            potentialPot = potentialPot,
            isMilestone = isMilestone,
            scoreResult = MatchScoreResult(
                finalScore = doubleDownResult.finalScore.coerceIn(0, MAX_SCORE).toInt(),
                ddBonus = doubleDownResult.bonus.coerceIn(0, MAX_SCORE).toInt(),
            ),
        )
    }

    private fun calculateMatchPoints(state: MemoryGameState): MatchPoints {
        val comboFactor = state.comboMultiplier.toLong() * state.comboMultiplier.toLong()
        val matchBasePoints = state.config.baseMatchPoints.toLong()
        val matchComboBonus = (comboFactor * state.config.comboBonusPoints)
            .coerceAtMost(MAX_SCORE)
        return MatchPoints(matchBasePoints, matchComboBonus)
    }

    private fun calculatePot(
        currentPot: Int,
        matchTotalPoints: Long,
    ): Long = (currentPot + matchTotalPoints).coerceAtMost(MAX_SCORE)

    private fun calculateScoreWithPot(
        state: MemoryGameState,
        potentialPot: Long,
        isWon: Boolean,
        isMilestone: Boolean,
    ): Long = if (isMilestone || isWon) {
        state.score + potentialPot
    } else {
        state.score.toLong()
    }

    private fun calculateDoubleDown(
        isWon: Boolean,
        state: MemoryGameState,
        scoreWithPot: Long,
    ): DoubleDownResult =
        if (isWon && state.isDoubleDownActive) {
            val doubledScore = scoreWithPot * DOUBLE_DOWN_MULTIPLIER
            DoubleDownResult(doubledScore, scoreWithPot)
        } else {
            DoubleDownResult(scoreWithPot, 0L)
        }

    /**
     * Calculates final bonuses (Time and Move Efficiency) when the game is won.
     */
    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState {
        if (!state.isGameWon) return state

        val bonusResult = calculateBonuses(state, elapsedTimeSeconds)

        return state.copy(
            score = bonusResult.totalScore,
            scoreBreakdown = ScoreBreakdown(
                basePoints = state.totalBasePoints,
                comboBonus = state.totalComboBonus,
                doubleDownBonus = state.totalDoubleDownBonus,
                timeBonus = bonusResult.timeBonus,
                moveBonus = bonusResult.moveBonus,
                dailyChallengeBonus = bonusResult.dailyChallengeBonus,
                totalScore = bonusResult.totalScore,
                earnedCurrency = bonusResult.earnedCurrency,
            ),
        )
    }

    private fun calculateBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): BonusResult {
        val config = state.config
        val timeBonus = calculateTimeBonus(state, elapsedTimeSeconds, config)
        val moveBonus = calculateMoveBonus(state, config)
        val totalScore = calculateTotalScore(state, timeBonus, moveBonus)
        val dailyChallengeBonus = calculateDailyChallengeBonus(state)
        val earnedCurrency = calculateEarnedCurrency(state, totalScore)

        return BonusResult(
            timeBonus = timeBonus,
            moveBonus = moveBonus,
            totalScore = totalScore,
            earnedCurrency = earnedCurrency,
            dailyChallengeBonus = dailyChallengeBonus,
        )
    }

    private fun calculateMoveBonus(
        state: MemoryGameState,
        config: ScoringConfig,
    ): Int {
        val effectiveMoves = state.moves.coerceAtLeast(MIN_EFFECTIVE_MOVES)
        val moveEfficiency = state.pairCount.toDouble() / effectiveMoves.toDouble()
        return (moveEfficiency * config.moveBonusMultiplier).toInt()
    }

    private fun calculateTotalScore(
        state: MemoryGameState,
        timeBonus: Int,
        moveBonus: Int,
    ): Int {
        val totalScoreLong = state.score.toLong() + timeBonus + moveBonus
        return totalScoreLong.coerceIn(0, MAX_SCORE).toInt()
    }

    private fun calculateDailyChallengeBonus(state: MemoryGameState): Int =
        if (state.mode == GameMode.DAILY_CHALLENGE) DAILY_CHALLENGE_CURRENCY_BONUS else 0

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
    ): Int {
        val baseCurrency = totalScore / CURRENCY_DIVISOR
        return if (state.mode == GameMode.DAILY_CHALLENGE) {
            baseCurrency + DAILY_CHALLENGE_CURRENCY_BONUS
        } else {
            (baseCurrency * state.difficulty.payoutMultiplier).toInt()
        }
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
