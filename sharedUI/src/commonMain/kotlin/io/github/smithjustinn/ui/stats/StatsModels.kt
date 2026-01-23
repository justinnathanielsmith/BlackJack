package io.github.smithjustinn.ui.stats

import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry

data class StatsState(
    val difficultyLeaderboards: List<Pair<DifficultyLevel, List<LeaderboardEntry>>> = emptyList(),
    val selectedGameMode: GameMode = GameMode.STANDARD
)

sealed class StatsUiEvent {
    data object PlayClick : StatsUiEvent()
}
