package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Breakdown of the final score.
 */
@Serializable
data class ScoreBreakdown(
    val matchPoints: Int = 0,
    val timeBonus: Int = 0,
    val moveBonus: Int = 0,
    val totalScore: Int = 0,
)
