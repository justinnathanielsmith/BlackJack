package io.github.smithjustinn.ui.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val SUBSCRIPTION_TIMEOUT_MS = 5000L

class DefaultSettingsComponent(
    componentContext: ComponentContext,
    appGraph: AppGraph,
    private val onBackClicked: () -> Unit,
) : SettingsComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val settingsRepository = appGraph.settingsRepository

    private val _events = Channel<SettingsUiEvent>(Channel.BUFFERED)
    override val events: Flow<SettingsUiEvent> = _events.receiveAsFlow()

    private val audioSettingsFlow =
        combine(
            settingsRepository.isSoundEnabled,
            settingsRepository.isMusicEnabled,
            settingsRepository.soundVolume,
            settingsRepository.musicVolume,
        ) { sound, music, soundVol, musicVol ->
            AudioSettings(sound, music, soundVol, musicVol)
        }

    private val themeSettingsFlow =
        combine(
            settingsRepository.cardBackTheme,
            settingsRepository.cardSymbolTheme,
            settingsRepository.areSuitsMultiColored,
        ) { back, symbol, multiColor ->
            ThemeSettings(back, symbol, multiColor)
        }

    override val state: StateFlow<SettingsState> =
        combine(
            settingsRepository.isPeekEnabled,
            settingsRepository.isWalkthroughCompleted,
            audioSettingsFlow,
            themeSettingsFlow,
        ) { peek, walkthrough, audio, theme ->
            SettingsState(
                isPeekEnabled = peek,
                isWalkthroughCompleted = walkthrough,
                isSoundEnabled = audio.isSoundEnabled,
                isMusicEnabled = audio.isMusicEnabled,
                soundVolume = audio.soundVolume,
                musicVolume = audio.musicVolume,
                cardBackTheme = theme.cardBackTheme,
                cardSymbolTheme = theme.cardSymbolTheme,
                areSuitsMultiColored = theme.areSuitsMultiColored,
            )
        }.stateIn(
            scope = scope,
            started = SharingStarted.WhileSubscribed(SUBSCRIPTION_TIMEOUT_MS),
            initialValue = SettingsState(),
        )

    private data class AudioSettings(
        val isSoundEnabled: Boolean,
        val isMusicEnabled: Boolean,
        val soundVolume: Float,
        val musicVolume: Float,
    )

    private data class ThemeSettings(
        val cardBackTheme: CardBackTheme,
        val cardSymbolTheme: CardSymbolTheme,
        val areSuitsMultiColored: Boolean,
    )

    init {
        lifecycle.doOnDestroy { scope.cancel() }
    }

    override fun togglePeekEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setPeekEnabled(enabled)
        }
    }

    override fun toggleSoundEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setSoundEnabled(enabled)
        }
    }

    override fun toggleMusicEnabled(enabled: Boolean) {
        scope.launch {
            settingsRepository.setMusicEnabled(enabled)
        }
    }

    override fun setSoundVolume(volume: Float) {
        scope.launch {
            settingsRepository.setSoundVolume(volume)
        }
    }

    override fun setMusicVolume(volume: Float) {
        scope.launch {
            settingsRepository.setMusicVolume(volume)
        }
    }

    override fun setCardBackTheme(theme: CardBackTheme) {
        scope.launch {
            settingsRepository.setCardBackTheme(theme)
        }
    }

    override fun setCardSymbolTheme(theme: CardSymbolTheme) {
        scope.launch {
            settingsRepository.setCardSymbolTheme(theme)
        }
    }

    override fun toggleSuitsMultiColored(enabled: Boolean) {
        scope.launch {
            settingsRepository.setSuitsMultiColored(enabled)
        }
    }

    override fun resetWalkthrough() {
        scope.launch {
            settingsRepository.setWalkthroughCompleted(false)
        }
    }

    override fun onBack() {
        onBackClicked()
    }
}

// Rename the UI state class if it doesn't match the interface
typealias SettingsState = SettingsUIState
