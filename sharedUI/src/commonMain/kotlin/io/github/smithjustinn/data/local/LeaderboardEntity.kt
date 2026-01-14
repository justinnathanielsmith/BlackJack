package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.smithjustinn.domain.models.GameMode
import kotlin.time.Instant

@Entity(tableName = "leaderboard")
data class LeaderboardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pairCount: Int,
    val score: Int,
    val timeSeconds: Long,
    val moves: Int,
    val timestamp: Instant,
    val gameMode: GameMode = GameMode.STANDARD
)
