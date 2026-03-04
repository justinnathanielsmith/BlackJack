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
) {
    init {
        require(basePoints >= 0) { "Base points cannot be negative" }
        require(comboBonus >= 0) { "Combo bonus cannot be negative" }
        require(doubleDownBonus >= 0) { "Double down bonus cannot be negative" }
        require(timeBonus >= 0) { "Time bonus cannot be negative" }
        require(moveBonus >= 0) { "Move bonus cannot be negative" }
        require(dailyChallengeBonus >= 0) { "Daily challenge bonus cannot be negative" }
        require(totalScore >= 0) { "Total score cannot be negative" }
        require(earnedCurrency >= 0) { "Earned currency cannot be negative" }
    }
}
