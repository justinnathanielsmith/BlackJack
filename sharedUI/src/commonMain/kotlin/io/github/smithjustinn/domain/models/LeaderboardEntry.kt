package io.github.smithjustinn.domain.models

import kotlin.time.Instant

/**
 * Domain model for a single leaderboard entry.
 */
data class LeaderboardEntry(
    val id: Long = 0,
    val pairCount: Int,
    val score: Int,
    val timeSeconds: Long,
    val moves: Int,
    val timestamp: Instant,
    val gameMode: GameMode = GameMode.STANDARD
)
