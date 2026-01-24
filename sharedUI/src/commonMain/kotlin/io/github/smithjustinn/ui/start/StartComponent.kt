package io.github.smithjustinn.ui.start

import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import kotlinx.coroutines.flow.StateFlow

interface StartComponent {
    val state: StateFlow<DifficultyState>

    fun onDifficultySelected(level: DifficultyLevel)
    fun onModeSelected(mode: GameMode)
    fun onStartGame()
    fun onResumeGame()
    fun onDailyChallengeClick()
    fun onSettingsClick()
    fun onStatsClick()
}
