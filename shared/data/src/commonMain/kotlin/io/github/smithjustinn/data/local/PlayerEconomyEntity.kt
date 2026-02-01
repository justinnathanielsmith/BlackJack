package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_economy")
data class PlayerEconomyEntity(
    @PrimaryKey
    val id: Int = 0, // Single row for player economy
    val balance: Long = 0,
    val unlockedItemIds: String = "", // Comma-separated list of unlocked item IDs
    val selectedThemeId: String = "GEOMETRIC",
)
