package io.github.smithjustinn.ui.start

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnResume
import io.github.smithjustinn.data.local.CircuitStatsEntity
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.CircuitStage
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

class DefaultStartComponent(
    componentContext: ComponentContext,
    private val appGraph: AppGraph,
    private val onNavigateToGame: (
        pairs: Int,
        mode: GameMode,
        forceNewGame: Boolean,
        stage: CircuitStage?,
        bankedScore: Int?,
        shouldOpenBuyIn: Boolean,
    ) -> Unit,
    private val onNavigateToSettings: () -> Unit,
    private val onNavigateToStats: () -> Unit,
) : StartComponent,
    ComponentContext by componentContext {
    private val dispatchers = appGraph.coroutineDispatchers
    private val scope = lifecycle.componentScope(dispatchers.mainImmediate)

    private val _state = MutableStateFlow(DifficultyState())
    override val state: StateFlow<DifficultyState> = _state.asStateFlow()

    private val gameStateRepository = appGraph.gameStateRepository
    private val settingsRepository = appGraph.settingsRepository
    private val dailyChallengeRepository = appGraph.dailyChallengeRepository
    private val logger = appGraph.logger

    init {

        scope.launch {
            combine(
                settingsRepository.cardBackTheme,
                settingsRepository.cardSymbolTheme,
                settingsRepository.areSuitsMultiColored,
            ) { cardBack, cardSymbol, multiColor ->
                _state.update {
                    it.copy(
                        cardSettings =
                            CardDisplaySettings(
                                backTheme = cardBack,
                                symbolTheme = cardSymbol,
                                areSuitsMultiColored = multiColor,
                            ),
                    )
                }
            }.collect()
        }

        lifecycle.doOnResume { checkSavedGame() }
        observeDailyChallengeStatus()
    }

    @Suppress("TooGenericExceptionCaught")
    private fun checkSavedGame() {
        scope.launch {
            try {
                val savedGame = gameStateRepository.getSavedGameState()
                val activeCircuit: CircuitStatsEntity? =
                    appGraph.appDatabase
                        .circuitStatsDao()
                        .getActiveCircuitRun()
                        .firstOrNull()

                _state.update { currentState ->
                    val runId = activeCircuit?.runId
                    val stage =
                        activeCircuit?.let { entity ->
                            CircuitStage.entries
                                .find { it.id == entity.currentStageId }
                        }
                    val banked = activeCircuit?.bankedScore ?: 0

                    currentState.copy(
                        hasSavedGame = savedGame != null && !savedGame.first.isGameOver,
                        savedGamePairCount = savedGame?.first?.pairCount ?: 0,
                        savedGameMode = savedGame?.first?.mode ?: GameMode.TIME_ATTACK,
                        activeCircuitRunId = runId,
                        activeCircuitStage = stage,
                        activeCircuitBankedScore = banked,
                    )
                }
            } catch (e: Exception) {
                logger.e(e) { "Error checking for saved game" }
            }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    private fun observeDailyChallengeStatus() {
        scope.launch {
            try {
                val today = Clock.System.now().toEpochMilliseconds() / MILLIS_PER_DAY

                dailyChallengeRepository.isChallengeCompleted(today).collect { isCompleted ->
                    _state.update { it.copy(isDailyChallengeCompleted = isCompleted) }
                }
            } catch (e: Exception) {
                logger.e(e) { "Error checking daily challenge status" }
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
        onNavigateToGame(pairs, mode, true, null, 0, mode == GameMode.HIGH_ROLLER)
    }

    override fun onResumeGame() {
        if (state.value.hasSavedGame && state.value.savedGameMode == GameMode.HIGH_ROLLER) {
            // Resume mid-game High Roller
            onNavigateToGame(
                state.value.savedGamePairCount,
                GameMode.HIGH_ROLLER,
                false,
                state.value.activeCircuitStage,
                state.value.activeCircuitBankedScore,
                false, // DO NOT open BuyIn, open Game
            )
        } else if (state.value.activeCircuitRunId != null) {
            // Resume mid-circuit High Roller (at BuyIn screen)
            onNavigateToGame(
                state.value.activeCircuitStage?.pairCount ?: 6,
                GameMode.HIGH_ROLLER,
                false,
                state.value.activeCircuitStage,
                state.value.activeCircuitBankedScore,
                true, // Open BuyIn
            )
        } else if (state.value.hasSavedGame) {
            onNavigateToGame(
                state.value.savedGamePairCount,
                state.value.savedGameMode,
                false,
                null,
                0,
                false,
            )
        }
    }

    override fun onDailyChallengeClick() {
        if (!state.value.isDailyChallengeCompleted) {
            // Use 8 pairs for Daily Challenge (standard difficulty)
            onNavigateToGame(DAILY_CHALLENGE_PAIRS, GameMode.DAILY_CHALLENGE, true, null, 0, false)
        }
    }

    override fun onSettingsClick() {
        onNavigateToSettings()
    }

    override fun onStatsClick() {
        onNavigateToStats()
    }

    override fun onEntranceAnimationCompleted() {
        _state.update { it.copy(shouldAnimateEntrance = false) }
    }

    companion object {
        private const val MILLIS_PER_DAY = 86_400_000L
        private const val DAILY_CHALLENGE_PAIRS = 8
    }
}
