package io.github.smithjustinn.ui.settings

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface SettingsComponent {
    val state: StateFlow<SettingsState>
    val events: Flow<SettingsUiEvent>

    fun toggleSoundEnabled(enabled: Boolean)

    fun setSoundVolume(volume: Float)

    fun toggleMusicEnabled(enabled: Boolean)

    fun setMusicVolume(volume: Float)

    fun togglePeekEnabled(enabled: Boolean)

    fun toggleSuitsMultiColored(enabled: Boolean)

    fun toggleThirdEyeEnabled(enabled: Boolean)

    fun toggleHeatShieldEnabled(enabled: Boolean)

    fun resetWalkthrough()

    fun onBack()
}
