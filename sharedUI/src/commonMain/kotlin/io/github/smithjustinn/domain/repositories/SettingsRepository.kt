package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import kotlinx.coroutines.flow.StateFlow

interface SettingsRepository {
    val isPeekEnabled: StateFlow<Boolean>
    val isSoundEnabled: StateFlow<Boolean>
    val isMusicEnabled: StateFlow<Boolean>
    val isWalkthroughCompleted: StateFlow<Boolean>
    val soundVolume: StateFlow<Float>
    val musicVolume: StateFlow<Float>
    val cardBackTheme: StateFlow<CardBackTheme>
    val cardSymbolTheme: StateFlow<CardSymbolTheme>
    val areSuitsMultiColored: StateFlow<Boolean>

    suspend fun setPeekEnabled(enabled: Boolean)

    suspend fun setSoundEnabled(enabled: Boolean)

    suspend fun setMusicEnabled(enabled: Boolean)

    suspend fun setWalkthroughCompleted(completed: Boolean)

    suspend fun setSoundVolume(volume: Float)

    suspend fun setMusicVolume(volume: Float)

    suspend fun setCardBackTheme(theme: CardBackTheme)

    suspend fun setCardSymbolTheme(theme: CardSymbolTheme)

    suspend fun setSuitsMultiColored(enabled: Boolean)
}
