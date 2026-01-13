package io.github.smithjustinn.ui.stats

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

data class StatsState(
    val difficultyLeaderboards: List<Pair<DifficultyLevel, List<LeaderboardEntry>>> = emptyList()
)

@Inject
class StatsScreenModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val logger: Logger
) : ScreenModel {
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    init {
        loadLeaderboards()
    }

    private fun loadLeaderboards() {
        val difficulties = DifficultyLevel.defaultLevels
        val flows = difficulties.map { level ->
            leaderboardRepository.getTopEntries(level.pairs).map { entries -> level to entries }
        }

        combine(flows) { pairs ->
            StatsState(difficultyLeaderboards = pairs.toList())
        }.onEach { newState ->
            _state.update { newState }
        }.catch { e ->
            logger.e(e) { "Error loading leaderboards" }
        }.launchIn(screenModelScope)
    }
}
