package io.github.smithjustinn.ui.stats

import io.github.smithjustinn.domain.models.GameMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface StatsComponent {
    val state: StateFlow<StatsState>
    val events: Flow<StatsUiEvent>

    fun onGameModeSelected(mode: GameMode)

    fun onBack()
}
