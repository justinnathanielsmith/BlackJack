package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey val id: Int = 0, // Single row for settings
    val isPeekEnabled: Boolean = true
)
