package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Configuration for game scoring and bonuses.
 */
@Serializable
data class ScoringConfig(
    val baseMatchPoints: Int = 20,
    val comboBonusPoints: Int = 100,
    val timeBonusPerPair: Int = 50,
    val timePenaltyPerSecond: Int = 1,
    val moveBonusMultiplier: Int = 10000, // Base for move efficiency
    val heatModeThreshold: Int = 3, // Combo level to activate heat mode
    val doubleDownPenalty: Int = 500, // Penalty for missing a Double Down
    val highRollerThreshold: Int = 1, // Combo level for "High Roller"
    val theNutsThreshold: Int = 4, // Combo level for "The Nuts" (Difficult!)
)
