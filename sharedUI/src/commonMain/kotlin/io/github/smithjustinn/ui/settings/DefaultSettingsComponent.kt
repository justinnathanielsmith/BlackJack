package io.github.smithjustinn.ui.settings

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
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

    override val state: StateFlow<SettingsState> =
        combine(
            settingsRepository.isPeekEnabled,
            settingsRepository.isWalkthroughCompleted,
            settingsRepository.areSuitsMultiColored,
            appGraph.playerEconomyRepository.unlockedItemIds,
            audioSettingsFlow,
        ) { peek, walkthrough, multiColor, unlocked, audio ->
            SettingsState(
                isPeekEnabled = peek,
                isWalkthroughCompleted = walkthrough,
                areSuitsMultiColored = multiColor,
                isFourColorUnlocked = unlocked.contains("feature_four_color_suits"),
                isSoundEnabled = audio.isSoundEnabled,
                isMusicEnabled = audio.isMusicEnabled,
                soundVolume = audio.soundVolume,
                musicVolume = audio.musicVolume,
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
