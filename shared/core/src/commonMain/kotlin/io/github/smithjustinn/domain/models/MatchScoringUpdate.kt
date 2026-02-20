package io.github.smithjustinn.domain.models

data class MatchScoringUpdate(
    val matchBasePoints: Int,
    val matchComboBonus: Int,
    val potentialPot: Long,
    val isMilestone: Boolean,
    val scoreResult: MatchScoreResult,
)
