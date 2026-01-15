package io.github.smithjustinn.ui.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SettingsUIState(
    val isPeekEnabled: Boolean = true
)

@Inject
class SettingsScreenModel(
    private val settingsRepository: SettingsRepository
) : ScreenModel {

    val state: StateFlow<SettingsUIState> = settingsRepository.isPeekEnabled
        .map { SettingsUIState(isPeekEnabled = it) }
        .stateIn(
            scope = screenModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsUIState()
        )

    fun togglePeekEnabled(enabled: Boolean) {
        screenModelScope.launch {
            settingsRepository.setPeekEnabled(enabled)
        }
    }
}
