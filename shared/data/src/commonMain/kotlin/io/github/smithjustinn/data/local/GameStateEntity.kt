package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.smithjustinn.domain.models.MemoryGameState

@Entity(tableName = "saved_game_state")
data class GameStateEntity(
    @PrimaryKey
    val id: Int = 0, // Only one saved game at a time
    val gameState: MemoryGameState,
    val elapsedTimeSeconds: Long,
)
