package io.github.smithjustinn.ui.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.MemoryGameLogic
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
            onGameOver = { processGameEnd() },
        )

    private val lifecycleHandler: GameLifecycleHandler =
        GameLifecycleHandler(
            appGraph = appGraph,
            state = _state,
            events = _events,
            timerHandler = timerHandler,
            onMatchFailure = { newState, isResuming ->
                feedbackHandler.handleMatchFailure(newState, isResuming)
            },
        )

    private val feedbackHandler: GameFeedbackHandler =
        GameFeedbackHandler(
            scope = scope,
            state = _state,
            events = _events,
            onGameOver = { processGameEnd() },
            onResetCards = { newState ->
                val resetState = appGraph.resetErrorCardsUseCase(newState)
                _state.update { it.copy(game = resetState) }
                lifecycleHandler.saveGame()
            },
        )

    init {
        lifecycle.doOnDestroy {
            timerHandler.stopTimer()
            lifecycleHandler.saveGame()
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

                appGraph.getGameStatsUseCase(args.pairCount).collect { stats ->
                    _state.update {
                        it.copy(
                            bestScore = stats?.bestScore ?: 0,
                            bestTimeSeconds = stats?.bestTimeSeconds ?: 0,
                            // Reset transient game state
                            isHeatMode = false,
                            isMegaBonus = false,
                            isNewHighScore = false,
                            showComboExplosion = false,
                            showTimeGain = false,
                            showTimeLoss = false,
                        )
                    }
                }
            } catch (
                @Suppress("TooGenericExceptionCaught") e: Exception,
            ) {
                appGraph.logger.e(e) { "Error starting game" }
            }
        }
    }

    private suspend fun setupNewGame(
        pairCount: Int,
        mode: GameMode,
        seed: Long?,
    ) {
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

    override fun onFlipCard(cardId: Int) {
        val currentState = _state.value
        if (currentState.isPeeking || currentState.game.isGameOver || currentState.showWalkthrough) return

        try {
            val (newState, event) = appGraph.flipCardUseCase(currentState.game, cardId)
            if (newState === currentState.game && event == null) return

            _state.update { it.copy(game = newState) }
            lifecycleHandler.saveGame()

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

                GameDomainEvent.TheNutsAchieved -> {
                    _events.tryEmit(GameUiEvent.PlayTheNuts)
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
                    processGameEnd(newState)
                }

                GameDomainEvent.GameOver -> {
                    processGameEnd()
                }

                else -> {}
            }
        } catch (
            @Suppress("TooGenericExceptionCaught") e: Exception,
        ) {
            appGraph.logger.e(e) { "Error flipping card: $cardId" }
        }
    }

    private fun processGameEnd(wonState: MemoryGameState? = null) {
        timerHandler.stopTimer()
        if (wonState != null) {
            val bonuses = appGraph.calculateFinalScoreUseCase(wonState, _state.value.elapsedTimeSeconds)
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
        } else {
            _state.update { it.copy(game = it.game.copy(isGameOver = true)) }
            _events.tryEmit(GameUiEvent.PlayLose)
            scope.launch { appGraph.clearSavedGameUseCase() }
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

    override fun onDoubleDown() {
        if (!_state.value.isHeatMode) return

        val currentState = _state.value
        val newState =
            MemoryGameLogic
                .activateDoubleDown(currentState.game)

        if (newState.isDoubleDownActive) {
            _state.update { it.copy(game = newState) }
            _events.tryEmit(GameUiEvent.VibrateHeat)
            // Reveal cards for 0.5s
            timerHandler.peekCards(currentState.game.mode, GameConstants.DOUBLE_DOWN_DURATION)
        }
    }
}
