package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "game_stats")
data class GameStatsEntity(
    @PrimaryKey val pairCount: Int,
    val bestScore: Int,
    val bestTimeSeconds: Long,
)
