package io.github.smithjustinn.ui.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SettingsUIState(
    val isPeekEnabled: Boolean = true,
    val isSoundEnabled: Boolean = true,
    val isMusicEnabled: Boolean = true,
    val isWalkthroughCompleted: Boolean = false,
    val soundVolume: Float = 1.0f,
    val musicVolume: Float = 1.0f
)

sealed class SettingsUiEvent {
    data object PlayClick : SettingsUiEvent()
}

@Inject
class SettingsScreenModel(
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    private val _events = Channel<SettingsUiEvent>(Channel.BUFFERED)
    val events: Flow<SettingsUiEvent> = _events.receiveAsFlow()

    private val audioSettingsFlow = combine(
        settingsRepository.isSoundEnabled,
        settingsRepository.isMusicEnabled,
        settingsRepository.soundVolume,
        settingsRepository.musicVolume
    ) { sound, music, soundVol, musicVol ->
        AudioSettings(sound, music, soundVol, musicVol)
    }

    val state: StateFlow<SettingsUIState> = combine(
        settingsRepository.isPeekEnabled,
        settingsRepository.isWalkthroughCompleted,
        audioSettingsFlow
    ) { peek, walkthrough, audio ->
        SettingsUIState(
            isPeekEnabled = peek,
            isWalkthroughCompleted = walkthrough,
            isSoundEnabled = audio.isSoundEnabled,
            isMusicEnabled = audio.isMusicEnabled,
            soundVolume = audio.soundVolume,
            musicVolume = audio.musicVolume
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUIState()
    )

    private data class AudioSettings(
        val isSoundEnabled: Boolean,
        val isMusicEnabled: Boolean,
        val soundVolume: Float,
        val musicVolume: Float
    )

    fun togglePeekEnabled(enabled: Boolean) {
        screenModelScope.launch {
            settingsRepository.setPeekEnabled(enabled)
        }
    }

    fun toggleSoundEnabled(enabled: Boolean) {
        screenModelScope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    fun toggleMusicEnabled(enabled: Boolean) {
        screenModelScope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    fun setSoundVolume(volume: Float) {
        screenModelScope.launch {
            settingsRepository.setSoundVolume(volume)
        }
    }

    fun setMusicVolume(volume: Float) {
        screenModelScope.launch {
            settingsRepository.setMusicVolume(volume)
        }
    }

    fun resetWalkthrough() {
        screenModelScope.launch {
            settingsRepository.setWalkthroughCompleted(false)
        }
    }
}
