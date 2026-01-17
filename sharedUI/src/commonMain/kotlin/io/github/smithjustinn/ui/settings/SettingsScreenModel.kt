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
    val isWalkthroughCompleted: Boolean = false
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

    val state: StateFlow<SettingsUIState> = combine(
        settingsRepository.isPeekEnabled,
        settingsRepository.isSoundEnabled,
        settingsRepository.isWalkthroughCompleted
    ) { peek, sound, walkthrough ->
        SettingsUIState(
            isPeekEnabled = peek,
            isSoundEnabled = sound,
            isWalkthroughCompleted = walkthrough
        )
    }.stateIn(
        scope = screenModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUIState()
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

    fun resetWalkthrough() {
        screenModelScope.launch {
            settingsRepository.setWalkthroughCompleted(false)
        }
    }
}
