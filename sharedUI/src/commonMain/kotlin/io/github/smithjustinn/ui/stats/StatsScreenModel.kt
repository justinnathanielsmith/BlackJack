package io.github.smithjustinn.ui.stats

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

data class StatsState(
    val difficultyLeaderboards: List<Pair<DifficultyLevel, List<LeaderboardEntry>>> = emptyList(),
    val selectedGameMode: GameMode = GameMode.STANDARD
)

sealed class StatsUiEvent {
    data object PlayClick : StatsUiEvent()
}

@Inject
class StatsScreenModel(
    private val leaderboardRepository: LeaderboardRepository,
    private val logger: Logger
) : ScreenModel {
    private val _state = MutableStateFlow(StatsState())
    val state: StateFlow<StatsState> = _state.asStateFlow()

    private val _events = Channel<StatsUiEvent>(Channel.BUFFERED)
    val events: Flow<StatsUiEvent> = _events.receiveAsFlow()

    private val _selectedGameMode = MutableStateFlow(GameMode.STANDARD)

    init {
        _selectedGameMode
            .flatMapLatest { mode ->
                val difficulties = DifficultyLevel.defaultLevels
                val flows = difficulties.map { level ->
                    leaderboardRepository.getTopEntries(level.pairs, mode).map { entries -> level to entries }
                }
                combine(flows) { pairs ->
                    StatsState(
                        difficultyLeaderboards = pairs.toList(),
                        selectedGameMode = mode
                    )
                }
            }
            .onEach { newState ->
                _state.update { newState }
            }
            .catch { e ->
                logger.e(e) { "Error loading leaderboards" }
            }
            .launchIn(screenModelScope)
    }

    fun onGameModeSelected(mode: GameMode) {
        _selectedGameMode.value = mode
    }
}
