package io.github.smithjustinn.ui.stats

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultStatsComponent(
    componentContext: ComponentContext,
    appGraph: AppGraph,
    private val onBackClicked: () -> Unit,
) : StatsComponent,
    ComponentContext by componentContext {
    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val leaderboardRepository = appGraph.leaderboardRepository
    private val logger = appGraph.logger

    private val _state = MutableStateFlow(StatsState())
    override val state: StateFlow<StatsState> = _state.asStateFlow()

    private val _events = Channel<StatsUiEvent>(Channel.BUFFERED)
    override val events: Flow<StatsUiEvent> = _events.receiveAsFlow()

    private val selectedGameMode = MutableStateFlow(GameMode.STANDARD)

    init {
        lifecycle.doOnDestroy { scope.cancel() }

        selectedGameMode
            .flatMapLatest { mode ->
                val difficulties = DifficultyLevel.defaultLevels
                val flows =
                    difficulties.map { level ->
                        leaderboardRepository.getTopEntries(level.pairs, mode).map { entries ->
                            level to
                                entries.toImmutableList()
                        }
                    }
                combine(flows) { pairs ->
                    StatsState(
                        difficultyLeaderboards = pairs.toList().toImmutableList(),
                        selectedGameMode = mode,
                    )
                }
            }.onEach { newState ->
                _state.update { newState }
            }.catch { e ->
                logger.e(e) { "Error loading leaderboards" }
            }.launchIn(scope)
    }

    override fun onGameModeSelected(mode: GameMode) {
        selectedGameMode.value = mode
    }

    override fun onBack() {
        onBackClicked()
    }
}
