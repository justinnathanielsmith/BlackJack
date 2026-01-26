package io.github.smithjustinn.ui.settings

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsComponent {
    val state: StateFlow<SettingsState>
    val events: Flow<SettingsUiEvent>

    fun setCardBackTheme(theme: CardBackTheme)

    fun setCardSymbolTheme(theme: CardSymbolTheme)

    fun toggleSuitsMultiColored(enabled: Boolean)

    fun toggleSoundEnabled(enabled: Boolean)

    fun setSoundVolume(volume: Float)

    fun toggleMusicEnabled(enabled: Boolean)

    fun setMusicVolume(volume: Float)

    fun togglePeekEnabled(enabled: Boolean)

    fun resetWalkthrough()

    fun onBack()
}
