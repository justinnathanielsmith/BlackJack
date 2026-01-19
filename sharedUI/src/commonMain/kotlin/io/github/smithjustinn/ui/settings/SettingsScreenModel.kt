package io.github.smithjustinn.ui.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
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
    val musicVolume: Float = 1.0f,
    val cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    val cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
    val areSuitsMultiColored: Boolean = false
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

    private val themeSettingsFlow = combine(
        settingsRepository.cardBackTheme,
        settingsRepository.cardSymbolTheme,
        settingsRepository.areSuitsMultiColored
    ) { back, symbol, multiColor ->
        ThemeSettings(back, symbol, multiColor)
    }

    val state: StateFlow<SettingsUIState> = combine(
        settingsRepository.isPeekEnabled,
        settingsRepository.isWalkthroughCompleted,
        audioSettingsFlow,
        themeSettingsFlow
    ) { peek, walkthrough, audio, theme ->
        SettingsUIState(
            isPeekEnabled = peek,
            isWalkthroughCompleted = walkthrough,
            isSoundEnabled = audio.isSoundEnabled,
            isMusicEnabled = audio.isMusicEnabled,
            soundVolume = audio.soundVolume,
            musicVolume = audio.musicVolume,
            cardBackTheme = theme.cardBackTheme,
            cardSymbolTheme = theme.cardSymbolTheme,
            areSuitsMultiColored = theme.areSuitsMultiColored
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

    private data class ThemeSettings(
        val cardBackTheme: CardBackTheme,
        val cardSymbolTheme: CardSymbolTheme,
        val areSuitsMultiColored: Boolean
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

    fun setCardBackTheme(theme: CardBackTheme) {
        screenModelScope.launch {
            settingsRepository.setCardBackTheme(theme)
        }
    }

    fun setCardSymbolTheme(theme: CardSymbolTheme) {
        screenModelScope.launch {
            settingsRepository.setCardSymbolTheme(theme)
        }
    }

    fun toggleSuitsMultiColored(enabled: Boolean) {
        screenModelScope.launch {
            settingsRepository.setSuitsMultiColored(enabled)
        }
    }

    fun resetWalkthrough() {
        screenModelScope.launch {
            settingsRepository.setWalkthroughCompleted(false)
        }
    }
}
