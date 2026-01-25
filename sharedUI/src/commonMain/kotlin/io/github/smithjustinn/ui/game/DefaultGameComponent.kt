package io.github.smithjustinn.ui.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock

data class GameArgs(val pairCount: Int, val mode: GameMode, val forceNewGame: Boolean, val seed: Long? = null)

class DefaultGameComponent(
    componentContext: ComponentContext,
    private val appGraph: AppGraph,
    private val args: GameArgs,
    private val onBackClicked: () -> Unit,
) : GameComponent,
    ComponentContext by componentContext {
    private val dispatchers = appGraph.coroutineDispatchers
    private val scope = lifecycle.componentScope(dispatchers.mainImmediate)

    private val _state = MutableStateFlow(GameUIState())
    override val state: StateFlow<GameUIState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameUiEvent>(extraBufferCapacity = 64)
    override val events: Flow<GameUiEvent> = _events.asSharedFlow()

    private val timerHandler =
        GameTimerHandler(
            scope = scope,
            state = _state,
            events = _events,
            onGameOver = { handleGameOver() },
        )

    private val feedbackHandler =
        GameFeedbackHandler(
            scope = scope,
            state = _state,
            events = _events,
            onGameOver = { handleGameOver() },
            onResetCards = { newState ->
                val resetState = appGraph.resetErrorCardsUseCase(newState)
                _state.update { it.copy(game = resetState) }
                saveGame()
            },
        )

    private val lifecycleHandler =
        GameLifecycleHandler(
            state = _state,
            events = _events,
            timerHandler = timerHandler,
            onMatchFailure = { newState, isResuming ->
                feedbackHandler.handleMatchFailure(newState, isResuming)
            },
        )

    init {
        lifecycle.doOnDestroy {
            timerHandler.stopTimer()
            saveGame()
        }

        scope.launch {
            val settings = appGraph.settingsRepository
            val coreSettingsFlow =
                combine(
                    settings.isPeekEnabled,
                    settings.isWalkthroughCompleted,
                    settings.isMusicEnabled,
                    settings.isSoundEnabled,
                ) { peek, walkthrough, music, sound ->
                    peek to walkthrough to music to sound
                }

            combine(
                coreSettingsFlow,
                settings.cardBackTheme,
                settings.cardSymbolTheme,
                settings.areSuitsMultiColored,
            ) { core, cardBack, cardSymbol, multiColor ->
                val (core1, sound) = core
                val (core2, music) = core1
                val (peek, walkthrough) = core2
                _state.update {
                    it.copy(
                        isPeekFeatureEnabled = peek,
                        showWalkthrough = !walkthrough,
                        isMusicEnabled = music,
                        isSoundEnabled = sound,
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

        startGame(args)
    }

    private fun startGame(args: GameArgs) {
        scope.launch {
            try {
                val savedGame = if (args.forceNewGame) null else appGraph.getSavedGameUseCase()
                if (savedGame != null && lifecycleHandler.isSavedGameValid(savedGame, args.pairCount, args.mode)) {
                    lifecycleHandler.resumeExistingGame(savedGame)
                } else {
                    setupNewGame(args.pairCount, args.mode, args.seed)
                }
                observeStats(args.pairCount)
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                appGraph.logger.e(e) { "Error starting game" }
            }
        }
    }

    private suspend fun setupNewGame(pairCount: Int, mode: GameMode, seed: Long?) {
        val finalSeed =
            seed ?: if (mode == GameMode.DAILY_CHALLENGE) {
                Clock.System.now().toEpochMilliseconds() / GameConstants.MILLIS_IN_DAY
            } else {
                null
            }

        val initialState = appGraph.startNewGameUseCase(pairCount, mode = mode, seed = finalSeed)
        lifecycleHandler.setupNewGameState(initialState, mode, pairCount)

        delay(GameConstants.SETTINGS_COLLECTION_DELAY)

        val currentState = _state.value
        when {
            currentState.showWalkthrough -> { /* Walkthrough handles timer */ }

            currentState.isPeekFeatureEnabled -> {
                timerHandler.peekCards(mode)
            }

            else -> {
                timerHandler.startTimer(mode)
            }
        }
    }

    private fun observeStats(pairCount: Int) {
        scope.launch {
            appGraph.getGameStatsUseCase(pairCount).collect { stats ->
                _state.update {
                    it.copy(
                        bestScore = stats?.bestScore ?: 0,
                        bestTimeSeconds = stats?.bestTimeSeconds ?: 0,
                    )
                }
            }
        }
    }

    override fun onFlipCard(cardId: Int) {
        val currentState = _state.value
        if (currentState.isPeeking || currentState.game.isGameOver || currentState.showWalkthrough) return

        try {
            val (newState, event) = appGraph.flipCardUseCase(currentState.game, cardId)
            if (newState === currentState.game && event == null) return

            _state.update { it.copy(game = newState) }
            saveGame()

            when (event) {
                GameDomainEvent.CardFlipped -> {
                    _events.tryEmit(GameUiEvent.PlayFlip)
                }

                GameDomainEvent.MatchSuccess -> {
                    feedbackHandler.handleMatchSuccess(
                        newState = newState,
                        isHeatMode = currentState.isHeatMode,
                        isNowInHeatMode = newState.comboMultiplier >= newState.config.heatModeThreshold,
                    )
                }

                GameDomainEvent.MatchFailure -> {
                    feedbackHandler.handleMatchFailure(newState, false)
                }

                GameDomainEvent.GameWon -> {
                    handleGameWon(newState)
                }

                GameDomainEvent.GameOver -> {
                    handleGameOver()
                }

                else -> {}
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            appGraph.logger.e(e) { "Error flipping card: $cardId" }
        }
    }

    private fun handleGameWon(newState: MemoryGameState) {
        timerHandler.stopTimer()
        val bonuses = appGraph.calculateFinalScoreUseCase(newState, _state.value.elapsedTimeSeconds)
        val isNewHigh = bonuses.score > _state.value.bestScore

        _events.tryEmit(GameUiEvent.VibrateMatch)
        _events.tryEmit(if (isNewHigh) GameUiEvent.PlayHighScore else GameUiEvent.PlayWin)
        _state.update { it.copy(game = bonuses, isNewHighScore = isNewHigh) }

        scope.launch {
            appGraph.saveGameResultUseCase(
                pairCount = bonuses.pairCount,
                score = bonuses.score,
                timeSeconds = _state.value.elapsedTimeSeconds,
                moves = bonuses.moves,
                gameMode = bonuses.mode,
            )
            if (bonuses.mode == GameMode.DAILY_CHALLENGE) {
                appGraph.dailyChallengeRepository.saveChallengeResult(
                    Clock.System.now().toEpochMilliseconds() / GameConstants.MILLIS_IN_DAY,
                    bonuses.score,
                    _state.value.elapsedTimeSeconds,
                    bonuses.moves,
                )
            }
            appGraph.clearSavedGameUseCase()
        }
        feedbackHandler.clearCommentAfterDelay()
    }

    private fun handleGameOver() {
        timerHandler.stopTimer()
        _state.update { it.copy(game = it.game.copy(isGameOver = true)) }
        _events.tryEmit(GameUiEvent.PlayLose)
        scope.launch { appGraph.clearSavedGameUseCase() }
    }

    private fun saveGame() {
        appGraph.applicationScope.launch(appGraph.coroutineDispatchers.io) {
            val currentState = _state.value
            if (!currentState.game.isGameOver && currentState.game.cards.isNotEmpty()) {
                appGraph.saveGameStateUseCase(currentState.game, currentState.elapsedTimeSeconds)
            }
        }
    }

    override fun onRestart() = startGame(args.copy(forceNewGame = true))

    override fun onBack() = onBackClicked()

    override fun onToggleAudio() {
        scope.launch {
            val enabled = !(_state.value.isMusicEnabled || _state.value.isSoundEnabled)
            appGraph.settingsRepository.setMusicEnabled(enabled)
            appGraph.settingsRepository.setSoundEnabled(enabled)
        }
    }

    override fun onWalkthroughAction(isComplete: Boolean) {
        if (isComplete) {
            scope.launch {
                _state.update { it.copy(showWalkthrough = false) }
                appGraph.settingsRepository.setWalkthroughCompleted(true)
                if (appGraph.settingsRepository.isPeekEnabled.first()) {
                    timerHandler.peekCards(_state.value.game.mode)
                } else {
                    timerHandler.startTimer(_state.value.game.mode)
                }
            }
        } else {
            _state.update { it.copy(walkthroughStep = it.walkthroughStep + 1) }
        }
    }
}
