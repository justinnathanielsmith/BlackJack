package io.github.smithjustinn.ui.start

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class DefaultStartComponent(
    componentContext: ComponentContext,
    appGraph: AppGraph,
    private val onNavigateToGame: (pairs: Int, mode: GameMode, forceNewGame: Boolean) -> Unit,
    private val onNavigateToSettings: () -> Unit,
    private val onNavigateToStats: () -> Unit
) : StartComponent, ComponentContext by componentContext {

    private val scope = CoroutineScope(Dispatchers.Main.immediate + SupervisorJob())

    private val _state = MutableStateFlow(DifficultyState())
    override val state: StateFlow<DifficultyState> = _state.asStateFlow()

    private val gameStateRepository = appGraph.gameStateRepository
    private val settingsRepository = appGraph.settingsRepository
    private val logger = appGraph.logger

    init {
        lifecycle.doOnDestroy { scope.cancel() }

        scope.launch {
            combine(
                settingsRepository.cardBackTheme,
                settingsRepository.cardSymbolTheme
            ) { cardBack, cardSymbol ->
                _state.update {
                    it.copy(
                        cardBackTheme = cardBack,
                        cardSymbolTheme = cardSymbol
                    )
                }
            }.collect()
        }

        checkSavedGame()
    }

    private fun checkSavedGame() {
        scope.launch {
            try {
                val savedGame = gameStateRepository.getSavedGameState()
                _state.update {
                    it.copy(
                        hasSavedGame = savedGame != null && !savedGame.first.isGameOver,
                        savedGamePairCount = savedGame?.first?.pairCount ?: 0,
                        savedGameMode = savedGame?.first?.mode ?: GameMode.STANDARD
                    )
                }
            } catch (e: Exception) {
                logger.e(e) { "Error checking for saved game" }
            }
        }
    }

    override fun onDifficultySelected(level: DifficultyLevel) {
        _state.update { it.copy(selectedDifficulty = level) }
    }

    override fun onModeSelected(mode: GameMode) {
        _state.update { it.copy(selectedMode = mode) }
    }

    override fun onStartGame() {
        val pairs = state.value.selectedDifficulty.pairs
        val mode = state.value.selectedMode
        onNavigateToGame(pairs, mode, true)
    }

    override fun onResumeGame() {
        if (state.value.hasSavedGame) {
            onNavigateToGame(
                state.value.savedGamePairCount,
                state.value.savedGameMode,
                false
            )
        }
    }

    override fun onSettingsClick() {
        onNavigateToSettings()
    }

    override fun onStatsClick() {
        onNavigateToStats()
    }
}
