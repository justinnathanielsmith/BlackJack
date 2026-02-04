package io.github.smithjustinn.ui.game

import com.arkivanov.decompose.ComponentContext
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.GameAction
import io.github.smithjustinn.domain.GameEffect
import io.github.smithjustinn.domain.GameStateMachine
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.TimeAttackLogic
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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

    private var gameSessionJob: Job? = null
    private val activeGameScope: CoroutineScope?
        get() = gameSessionJob?.let { CoroutineScope(scope.coroutineContext + it) }

    private val _state = MutableStateFlow(GameUIState())
    override val state: StateFlow<GameUIState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<GameUiEvent>(extraBufferCapacity = 64)
    override val events: Flow<GameUiEvent> = _events.asSharedFlow()

    private var gameStateMachine: GameStateMachine? = null

    init {
        scope.launch {
            val settings = appGraph.settingsRepository
            combine(
                settings.isPeekEnabled,
                settings.isWalkthroughCompleted,
                settings.isMusicEnabled,
                settings.isSoundEnabled,
            ) { peek, walkthrough, music, sound ->
                _state.update {
                    it.copy(
                        isPeekFeatureEnabled = peek,
                        showWalkthrough = !walkthrough,
                        isMusicEnabled = music,
                        isSoundEnabled = sound,
                    )
                }
            }.first() // Wait for first emission

            startGame(args)

            val shopItems = appGraph.shopItemRepository.getShopItems()

            combine(
                appGraph.playerEconomyRepository.selectedThemeId,
                appGraph.playerEconomyRepository.selectedSkin,
                appGraph.settingsRepository.areSuitsMultiColored,
            ) { themeId: String, skin: CardSymbolTheme, multiColor: Boolean ->
                val theme = CardBackTheme.fromIdOrName(themeId)
                val hexColor = shopItems.find { item -> item.id == themeId }?.hexColor
                _state.update {
                    it.copy(
                        cardTheme = CardTheme(back = theme, skin = skin, backColorHex = hexColor),
                        areSuitsMultiColored = multiColor,
                    )
                }
            }.collect()
        }
    }

    private fun startGame(args: GameArgs) {
        // Cancel any existing game session to prevent state/timer leaks
        gameSessionJob?.cancel()
        val newJob = Job(scope.coroutineContext[Job])
        gameSessionJob = newJob
        // Create a temporary scope reference for initialization
        val gameScope = CoroutineScope(scope.coroutineContext + newJob)

        gameScope.launch {
            try {
                loadGameStats(args.pairCount)
                val initResult = initializeGameState(args)
                setupUIState(initResult.state, initResult.initialTime, args)
                startStateMachine(initResult.state, initResult.initialTime)
                handleGameStartSequence(initResult.isResumed)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IllegalStateException) {
                appGraph.logger.e(e) { "Error starting game" }
            }
        }
    }

    private fun loadGameStats(pairCount: Int) {
        activeGameScope?.launch {
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

    private suspend fun initializeGameState(args: GameArgs): GameInitResult {
        val savedGame = if (args.forceNewGame) null else appGraph.getSavedGameUseCase()
        return if (savedGame != null && isSavedGameValid(savedGame, args.pairCount, args.mode)) {
            GameInitResult(savedGame.gameState, savedGame.elapsedTimeSeconds, isResumed = true)
        } else {
            val newState = setupNewGame(args.pairCount, args.mode, args.difficulty, args.seed)
            val initialTime =
                if (args.mode == GameMode.TIME_ATTACK) {
                    TimeAttackLogic.calculateInitialTime(args.pairCount, newState.config)
                } else {
                    0L
                }
            GameInitResult(newState, initialTime, isResumed = false)
        }
    }

    private fun setupUIState(
        initialState: MemoryGameState,
        initialTime: Long,
        args: GameArgs,
    ) {
        val maxTime =
            if (args.mode == GameMode.TIME_ATTACK) {
                TimeAttackLogic.calculateInitialTime(args.pairCount, initialState.config)
            } else {
                0L
            }
        _state.update {
            it.copy(
                game = initialState,
                elapsedTimeSeconds = initialTime,
                maxTimeSeconds = maxTime,
                isHeatMode = initialState.comboMultiplier >= initialState.config.heatModeThreshold,
            )
        }
    }

    private fun startStateMachine(
        initialState: MemoryGameState,
        initialTime: Long,
    ) {
        val sessionScope = activeGameScope ?: return

        gameStateMachine =
            GameStateMachine(
                scope = sessionScope,
                dispatchers = dispatchers,
                initialState = initialState,
                initialTimeSeconds = initialTime,
                earnCurrencyUseCase = appGraph.earnCurrencyUseCase,
                onSaveState = { state, time -> saveGame(state, time) },
            ).also { machine ->
                sessionScope.launch {
                    machine.state.collectLatest { gameState ->
                        _state.update { it.copy(game = gameState) }
                    }
                }
                sessionScope.launch {
                    machine.state
                        .map { it.matchComment }
                        .distinctUntilChanged()
                        .collectLatest { comment ->
                            if (comment != null) {
                                delay(GameConstants.COMMENT_DURATION_MS)
                                machine.dispatch(GameAction.ClearComment)
                            }
                        }
                }
                sessionScope.launch { machine.effects.collect { effect -> handleEffect(effect) } }
            }
    }

    private fun handleGameStartSequence(isResumed: Boolean) {
        val currentState = _state.value
        when {
            currentState.showWalkthrough -> { /* Walkthrough handles it */ }

            currentState.isPeekFeatureEnabled && !isResumed -> {
                startPeekSequence()
            }

            else -> {
                gameStateMachine?.dispatch(GameAction.StartGame())
            }
        }
    }

    private fun startPeekSequence() {
        activeGameScope?.launch {
            _state.update {
                it.copy(isPeeking = true, peekCountdown = GameConstants.PEEK_DURATION_SECONDS)
            }
            for (i in GameConstants.PEEK_DURATION_SECONDS downTo 1) {
                _state.update { it.copy(peekCountdown = i) }
                _events.tryEmit(GameUiEvent.VibrateTick)
                delay(GameConstants.PEEK_COUNTDOWN_DELAY_MS)
            }
            _state.update { it.copy(isPeeking = false, peekCountdown = 0) }
            _events.emit(GameUiEvent.PlayFlip)
            gameStateMachine?.dispatch(GameAction.StartGame())
        }
    }

    private suspend fun setupNewGame(
        pairCount: Int,
        mode: GameMode,
        difficulty: DifficultyType,
        seed: Long?,
    ): MemoryGameState {
        val finalSeed =
            seed ?: if (mode == GameMode.DAILY_CHALLENGE) {
                Clock.System.now().toEpochMilliseconds() / GameConstants.MILLIS_IN_DAY
            } else {
                null
            }

        val initialState =
            appGraph
                .startNewGameUseCase(pairCount, mode = mode, difficulty = difficulty, seed = finalSeed)

        _events.tryEmit(GameUiEvent.PlayDeal)
        return initialState
    }

    private fun handleEffect(effect: GameEffect) {
        // Handle simple event mappings first
        effectToEventMap[effect]?.let { event ->
            _events.tryEmit(event)
            return
        }

        // Handle complex effects that need additional logic
        when (effect) {
            is GameEffect.TimerUpdate -> {
                handleTimerUpdate(effect)
            }

            is GameEffect.TimeGain -> {
                handleTimeGain(effect)
            }

            is GameEffect.TimeLoss -> {
                handleTimeLoss(effect)
            }

            GameEffect.PlayMatchSound -> {
                handleMatchSound()
            }

            GameEffect.GameOver -> {
                handleGameLost()
            }

            is GameEffect.GameWon -> {
                handleGameWon(effect.finalState)
            }

            else -> { /* Already handled by effectToEventMap */ }
        }
    }

    private val effectToEventMap =
        mapOf(
            GameEffect.PlayFlipSound to GameUiEvent.PlayFlip,
            GameEffect.PlayTheNutsSound to GameUiEvent.PlayTheNuts,
            GameEffect.PlayWinSound to GameUiEvent.PlayWin,
            GameEffect.PlayLoseSound to GameUiEvent.PlayLose,
            GameEffect.PlayMismatch to GameUiEvent.PlayMismatch,
            GameEffect.VibrateMatch to GameUiEvent.VibrateMatch,
            GameEffect.VibrateMismatch to GameUiEvent.VibrateMismatch,
            GameEffect.VibrateHeat to GameUiEvent.VibrateHeat,
            GameEffect.VibrateWarning to GameUiEvent.VibrateWarning,
            GameEffect.VibrateTick to GameUiEvent.VibrateTick,
        )

    private fun handleTimerUpdate(effect: GameEffect.TimerUpdate) {
        _state.update { it.copy(elapsedTimeSeconds = effect.seconds) }
    }

    private fun handleTimeGain(effect: GameEffect.TimeGain) {
        val currentCombo = _state.value.game.comboMultiplier
        val heatThreshold = _state.value.game.config.heatModeThreshold
        _state.update {
            it.copy(
                showTimeGain = true,
                timeGainAmount = effect.amount,
                isMegaBonus = currentCombo >= GameConstants.MEGA_BONUS_THRESHOLD,
                isHeatMode = currentCombo >= heatThreshold,
            )
        }
        scope.launch {
            delay(GameConstants.UI_FEEDBACK_DURATION_MS)
            _state.update { it.copy(showTimeGain = false) }
        }
    }

    private fun handleTimeLoss(effect: GameEffect.TimeLoss) {
        _state.update {
            it.copy(
                showTimeLoss = true,
                timeLossAmount = effect.amount.toLong(),
                isHeatMode = false, // Combo is broken on mismatch
            )
        }
        scope.launch {
            delay(GameConstants.UI_FEEDBACK_DURATION_MS)
            _state.update { it.copy(showTimeLoss = false) }
        }
    }

    private fun handleMatchSound() {
        _events.tryEmit(GameUiEvent.PlayMatch)
        if (_state.value.game.comboMultiplier > GameConstants.COMBO_EXPLOSION_THRESHOLD) {
            scope.launch {
                _state.update { it.copy(showComboExplosion = true) }
                delay(GameConstants.COMBO_EXPLOSION_DURATION_MS)
                _state.update { it.copy(showComboExplosion = false) }
            }
        }
    }

    override fun onFlipCard(cardId: Int) {
        val currentState = _state.value
        if (currentState.isPeeking || currentState.game.isGameOver || currentState.showWalkthrough) return
        gameStateMachine?.dispatch(GameAction.FlipCard(cardId))
    }

    override fun onDoubleDown() {
        if (!_state.value.isHeatMode) return

        // UI Check for eligibility to avoid unnecessary dispatch?
        val game = _state.value.game
        val unmatchedPairs = game.cards.count { !it.isMatched } / 2
        if (unmatchedPairs < MemoryGameLogic.MIN_PAIRS_FOR_DOUBLE_DOWN) return

        if (!game.isDoubleDownActive && !_state.value.hasUsedDoubleDownPeek) {
            _state.update { it.copy(hasUsedDoubleDownPeek = true) }
        }

        gameStateMachine?.dispatch(GameAction.DoubleDown)
    }

    private fun handleGameWon(wonState: MemoryGameState) {
        val isNewHigh = wonState.score > _state.value.bestScore

        if (isNewHigh) {
            _events.tryEmit(GameUiEvent.PlayHighScore)
        }

        _state.update { it.copy(game = wonState, isNewHighScore = isNewHigh) }

        scope.launch {
            appGraph.saveGameResultUseCase(
                pairCount = wonState.pairCount,
                score = wonState.score,
                timeSeconds = _state.value.elapsedTimeSeconds, // Or from wonState / internal stat
                moves = wonState.moves,
                gameMode = wonState.mode,
            )

            handleDailyChallenge(wonState)
            appGraph.clearSavedGameUseCase()
        }
    }

    private fun handleGameLost() {
        // Game Over logic already mostly handled by state update in Machine
        // Helper to clear save
        scope.launch {
            val game = _state.value.game

            appGraph.clearSavedGameUseCase()
        }
    }

    private suspend fun handleDailyChallenge(bonuses: MemoryGameState) {
        if (bonuses.mode == GameMode.DAILY_CHALLENGE) {
            appGraph.dailyChallengeRepository.saveChallengeResult(
                Clock.System.now().toEpochMilliseconds() / GameConstants.MILLIS_IN_DAY,
                bonuses.score,
                _state.value.elapsedTimeSeconds,
                bonuses.moves,
            )
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
                    startPeekSequence()
                } else {
                    gameStateMachine?.dispatch(GameAction.StartGame())
                }
            }
        } else {
            _state.update { it.copy(walkthroughStep = it.walkthroughStep + 1) }
        }
    }

    private fun isSavedGameValid(
        savedGame: SavedGame,
        pairCount: Int,
        mode: GameMode,
    ): Boolean =
        savedGame.gameState.pairCount == pairCount &&
            !savedGame.gameState.isGameOver &&
            savedGame.gameState.mode == mode

    private fun saveGame(
        game: MemoryGameState,
        time: Long,
    ) {
        appGraph.applicationScope.launch(appGraph.coroutineDispatchers.io) {
            if (!game.isGameOver && game.cards.isNotEmpty()) {
                appGraph.saveGameStateUseCase(game, time)
            }
        }
    }

    private data class GameInitResult(
        val state: MemoryGameState,
        val initialTime: Long,
        val isResumed: Boolean,
    )
}
