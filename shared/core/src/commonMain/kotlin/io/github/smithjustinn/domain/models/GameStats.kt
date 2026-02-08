package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

/**
 * Domain model for game statistics.
 */
@Serializable
data class GameStats(
    val pairCount: Int,
    val bestScore: Int,
    val bestTimeSeconds: Long,
    val gamesPlayed: Int = 0,
) {
    init {
        require(pairCount > 0) { "Pair count must be positive" }
        require(bestScore >= 0) { "Best score cannot be negative" }
        require(bestTimeSeconds >= 0) { "Best time cannot be negative" }
        require(gamesPlayed >= 0) { "Games played cannot be negative" }
    }
}
