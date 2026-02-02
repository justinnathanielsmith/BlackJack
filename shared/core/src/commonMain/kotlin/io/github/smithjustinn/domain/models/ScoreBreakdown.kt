package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Breakdown of the final score.
 */
@Serializable
data class ScoreBreakdown(
    val basePoints: Int = 0,
    val comboBonus: Int = 0,
    val doubleDownBonus: Int = 0,
    val timeBonus: Int = 0,
    val moveBonus: Int = 0,
    val dailyChallengeBonus: Int = 0,
    val totalScore: Int = 0,
    val earnedCurrency: Int = 0,
)
