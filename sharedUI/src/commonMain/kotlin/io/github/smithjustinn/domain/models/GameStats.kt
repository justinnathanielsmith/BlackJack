package io.github.smithjustinn.domain.models

/**
 * Domain model for game statistics.
 */
data class GameStats(
    val pairCount: Int,
    val bestScore: Int,
    val bestTimeSeconds: Long,
)
