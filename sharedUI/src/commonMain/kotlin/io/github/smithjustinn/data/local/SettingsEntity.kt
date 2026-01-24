package io.github.smithjustinn.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme

@Entity(tableName = "settings")
data class SettingsEntity(
    @PrimaryKey
    val id: Int = 0, // Single row for settings
    val isPeekEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isWalkthroughCompleted: Boolean = false,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 1.0f,
    val cardBackTheme: String = CardBackTheme.GEOMETRIC.name,
    val cardSymbolTheme: String = CardSymbolTheme.CLASSIC.name,
    val areSuitsMultiColored: Boolean = false,
)
