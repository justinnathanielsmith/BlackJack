package io.github.smithjustinn.ui.stats

import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class StatsState(
    val difficultyLeaderboards: ImmutableList<Pair<DifficultyLevel, ImmutableList<LeaderboardEntry>>> =
        persistentListOf(),
    val selectedGameMode: GameMode = GameMode.STANDARD,
)

sealed class StatsUiEvent {
    data object PlayClick : StatsUiEvent()
}
