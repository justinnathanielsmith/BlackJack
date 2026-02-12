package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * Domain model for a single leaderboard entry.
 */
@Serializable
data class LeaderboardEntry(
    val id: Long = 0,
    val pairCount: Int,
    val score: Int,
    val timeSeconds: Long,
    val moves: Int,
    val timestamp: Instant,
    val gameMode: GameMode = GameMode.TIME_ATTACK,
) {
    init {
        require(pairCount > 0) { "Pair count must be positive" }
        require(score >= 0) { "Score cannot be negative" }
        require(timeSeconds >= 0) { "Time cannot be negative" }
        require(moves >= 0) { "Moves cannot be negative" }
    }
}
