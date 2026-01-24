package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_game_state")
data class GameStateEntity(
    @PrimaryKey
    val id: Int = 0, // Only one saved game at a time
    val gameStateJson: String,
    val elapsedTimeSeconds: Long,
)
