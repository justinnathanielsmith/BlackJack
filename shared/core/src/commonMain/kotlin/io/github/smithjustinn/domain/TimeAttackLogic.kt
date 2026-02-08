package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.ScoringConfig

/**
 * Logic specific to Time Attack mode.
 */
object TimeAttackLogic {
    /**
     * Calculates the initial time for Time Attack mode based on the pair count and config.
     */
    fun calculateInitialTime(
        pairCount: Int,
        config: ScoringConfig,
    ): Long = config.timeAttackInitialTimeMap[pairCount] ?: (pairCount * TIME_PER_PAIR_FALLBACK)

    /**
     * Logic for calculating time gain based on combo and config.
     */
    fun calculateTimeGain(
        comboMultiplier: Int,
        config: ScoringConfig,
    ): Int {
        val baseGain = config.timeAttackBaseGain
        val comboBonus = (comboMultiplier - 1).coerceAtLeast(0) * config.timeAttackComboBonusMultiplier
        return baseGain + comboBonus
    }

    /**
     * Adds bonus time from a rewarded ad.
     * @param currentTime The current elapsed time in seconds
     * @param bonusSeconds The number of bonus seconds to add (typically from ad reward)
     * @return The new elapsed time after applying the bonus
     */
    fun addBonusTime(
        currentTime: Long,
        bonusSeconds: Int,
    ): Long = (currentTime - bonusSeconds).coerceAtLeast(0)

    private const val TIME_PER_PAIR_FALLBACK = 4L
}
