package io.github.smithjustinn.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_economy")
data class PlayerEconomyEntity(
    @PrimaryKey
    val id: Int = 0, // Single row for player economy
    val balance: Long = 0,
    @ColumnInfo(name = "owned_items")
    val unlockedItemIds: String = "theme_standard,skin_classic", // Comma-separated list of unlocked item IDs
    @ColumnInfo(name = "active_theme")
    val selectedThemeId: String = "theme_standard",
    @ColumnInfo(name = "active_skin")
    val selectedSkinId: String = "skin_classic",
)
