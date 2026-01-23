package io.github.smithjustinn.ui.game

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.doOnDestroy
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class DefaultGameComponent(
    componentContext: ComponentContext,
    appGraph: AppGraph,
    private val pairCount: Int,
    private val mode: GameMode,
    forceNewGame: Boolean,
    private val onBackClicked: () -> Unit
) : GameComponent, ComponentContext by componentContext {
    private val dispatchers = appGraph.coroutineDispatchers
    private val scope = lifecycle.componentScope(dispatchers.mainImmediate)

    private val _state = MutableStateFlow(GameUIState())
    override val state: StateFlow<GameUIState> = _state.asStateFlow()

    private val _events = Channel<GameUiEvent>(Channel.BUFFERED)
    override val events: Flow<GameUiEvent> = _events.receiveAsFlow()

    private var timerJob: Job? = null
    private var commentJob: Job? = null
    private var statsJob: Job? = null
    private var explosionJob: Job? = null
    private var peekJob: Job? = null
    private var timeGainJob: Job? = null
    private var timeLossJob: Job? = null
    private var settingsJob: Job? = null

    private val startNewGameUseCase = appGraph.startNewGameUseCase
    private val flipCardUseCase = appGraph.flipCardUseCase
    private val resetErrorCardsUseCase = appGraph.resetErrorCardsUseCase
    private val calculateFinalScoreUseCase = appGraph.calculateFinalScoreUseCase
    private val getGameStatsUseCase = appGraph.getGameStatsUseCase
    private val saveGameResultUseCase = appGraph.saveGameResultUseCase
    private val getSavedGameUseCase = appGraph.getSavedGameUseCase
    private val saveGameStateUseCase = appGraph.saveGameStateUseCase
    private val clearSavedGameUseCase = appGraph.clearSavedGameUseCase
    private val settingsRepository = appGraph.settingsRepository
    private val logger = appGraph.logger

    private data class CoreSettings(
        val isPeekEnabled: Boolean,
        val isWalkthroughCompleted: Boolean,
        val isMusicEnabled: Boolean,
        val isSoundEnabled: Boolean
    )

    init {
        lifecycle.doOnDestroy { 
            stopTimer()
            saveGame()
        }

        settingsJob = scope.launch {
            val coreSettingsFlow = combine(
                settingsRepository.isPeekEnabled,
                settingsRepository.isWalkthroughCompleted,
                settingsRepository.isMusicEnabled,
                settingsRepository.isSoundEnabled
            ) { peek, walkthrough, music, sound ->
                CoreSettings(peek, walkthrough, music, sound)
            }

            combine(
                coreSettingsFlow,
                settingsRepository.cardBackTheme,
                settingsRepository.cardSymbolTheme,
                settingsRepository.areSuitsMultiColored
            ) { core, cardBack, cardSymbol, multiColor ->
                _state.update { it.copy(
                    isPeekFeatureEnabled = core.isPeekEnabled,
                    showWalkthrough = !core.isWalkthroughCompleted,
                    isMusicEnabled = core.isMusicEnabled,
                    isSoundEnabled = core.isSoundEnabled,
                    cardBackTheme = cardBack,
                    cardSymbolTheme = cardSymbol,
                    areSuitsMultiColored = multiColor
                ) }
            }.collect()
        }

        startGame(pairCount, forceNewGame, mode)
    }

    private fun startGame(pairCount: Int, forceNewGame: Boolean, mode: GameMode) {
        scope.launch {
            try {
                val savedGame = if (forceNewGame) null else getSavedGameUseCase()
                if (savedGame != null && savedGame.first.pairCount == pairCount && !savedGame.first.isGameOver && savedGame.first.mode == mode) {
                    val initialTime = if (mode == GameMode.TIME_ATTACK) MemoryGameLogic.calculateInitialTime(pairCount) else 0L
                    _state.update {
                        it.copy(
                            game = savedGame.first.copy(lastMatchedIds = emptyList()),
                            elapsedTimeSeconds = savedGame.second,
                            maxTimeSeconds = initialTime,
                            showComboExplosion = false,
                            isNewHighScore = false,
                            isPeeking = false,
                            showTimeGain = false,
                            showTimeLoss = false
                        )
                    }
                    startTimer(mode)

                    if (savedGame.first.cards.any { it.isError }) {
                        handleMatchFailure(savedGame.first, isResuming = true)
                    }
                } else {
                    val initialGameState = startNewGameUseCase(pairCount, mode = mode)
                    val initialTime = if (mode == GameMode.TIME_ATTACK) MemoryGameLogic.calculateInitialTime(pairCount) else 0L
                    
                    _state.update {
                        it.copy(
                            game = initialGameState,
                            elapsedTimeSeconds = initialTime,
                            maxTimeSeconds = initialTime,
                            showComboExplosion = false,
                            isNewHighScore = false,
                            isPeeking = false,
                            showTimeGain = false,
                            showTimeLoss = false
                        )
                    }
                    
                    _events.send(GameUiEvent.PlayDeal)

                    val walkthroughCompleted = settingsRepository.isWalkthroughCompleted.first()
                    if (walkthroughCompleted) {
                        val isPeekEnabled = settingsRepository.isPeekEnabled.first()
                        if (isPeekEnabled) {
                            peekCards(mode)
                        } else {
                            startTimer(mode)
                        }
                    }
                }

                observeStats(pairCount)
            } catch (e: Exception) {
                logger.e(e) { "Error starting game" }
            }
        }
    }

    private fun peekCards(mode: GameMode) {
        peekJob?.cancel()
        peekJob = scope.launch {
            stopTimer()
            val peekDuration = 3
            _state.update { it.copy(isPeeking = true, peekCountdown = peekDuration) }
            
            for (i in peekDuration downTo 1) {
                _state.update { it.copy(peekCountdown = i) }
                _events.send(GameUiEvent.VibrateTick)
                delay(1000)
            }
            
            _state.update { it.copy(isPeeking = false, peekCountdown = 0) }
            _events.send(GameUiEvent.PlayFlip)
            startTimer(mode)
        }
    }

    private fun observeStats(pairCount: Int) {
        statsJob?.cancel()
        statsJob = scope.launch {
            getGameStatsUseCase(pairCount).collect { stats ->
                _state.update { it.copy(
                    bestScore = stats?.bestScore ?: 0,
                    bestTimeSeconds = stats?.bestTimeSeconds ?: 0
                ) }
            }
        }
    }

    private fun startTimer(mode: GameMode) {
        timerJob?.cancel()
        timerJob = scope.launch {
            while (isActive) {
                delay(1000)
                if (mode == GameMode.TIME_ATTACK) {
                    var shouldStop = false
                    _state.update { 
                        val newTime = (it.elapsedTimeSeconds - 1).coerceAtLeast(0)
                        if (newTime == 0L && !it.game.isGameOver) {
                            shouldStop = true
                        }
                        
                        if (newTime in 1L..5L && !it.game.isGameOver) {
                            _events.trySend(GameUiEvent.VibrateTick)
                        }

                        it.copy(elapsedTimeSeconds = newTime)
                    }
                    if (shouldStop) {
                        _events.send(GameUiEvent.VibrateWarning)
                        handleGameOver()
                        break
                    }
                } else {
                    _state.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    override fun onFlipCard(cardId: Int) {
        if (_state.value.isPeeking || _state.value.game.isGameOver || _state.value.showWalkthrough) return

        try {
            val (newState, event) = flipCardUseCase(_state.value.game, cardId)

            _state.update { it.copy(game = newState) }
            saveGame()

            when (event) {
                GameDomainEvent.CardFlipped -> scope.launch { _events.send(GameUiEvent.PlayFlip) }
                GameDomainEvent.MatchSuccess -> handleMatchSuccess(newState)
                GameDomainEvent.MatchFailure -> handleMatchFailure(newState)
                GameDomainEvent.GameWon -> handleGameWon(newState)
                GameDomainEvent.GameOver -> handleGameOver()
                else -> {}
            }
        } catch (e: Exception) {
            logger.e(e) { "Error flipping card: $cardId" }
        }
    }

    private fun handleMatchSuccess(newState: MemoryGameState) {
        scope.launch {
            _events.send(GameUiEvent.VibrateMatch)
            _events.send(GameUiEvent.PlayMatch)
        }
        clearCommentAfterDelay()
        
        if (newState.mode == GameMode.TIME_ATTACK) {
            val totalTimeGain = MemoryGameLogic.calculateTimeGain(newState.comboMultiplier - 1)
            val isMega = newState.comboMultiplier >= 3

            _state.update { 
                it.copy(
                    elapsedTimeSeconds = it.elapsedTimeSeconds + totalTimeGain,
                    showTimeGain = true,
                    timeGainAmount = totalTimeGain,
                    isMegaBonus = isMega
                )
            }
            triggerTimeGainFeedback()
        }

        if (newState.comboMultiplier > 2) {
            triggerComboExplosion()
        }
    }

    private fun triggerTimeGainFeedback() {
        timeGainJob?.cancel()
        timeGainJob = scope.launch {
            delay(1500)
            _state.update { it.copy(showTimeGain = false) }
        }
    }

    private fun handleMatchFailure(newState: MemoryGameState, isResuming: Boolean = false) {
        if (!isResuming) {
            scope.launch {
                _events.send(GameUiEvent.VibrateMismatch)
                _events.send(GameUiEvent.PlayMismatch)
            }
        }
        
        var isGameOver = false
        if (newState.mode == GameMode.TIME_ATTACK && !isResuming) {
            val penalty = MemoryGameLogic.TIME_PENALTY_MISMATCH
            _state.update { 
                val newTime = (it.elapsedTimeSeconds - penalty).coerceAtLeast(0)
                if (newTime == 0L) isGameOver = true
                it.copy(
                    elapsedTimeSeconds = newTime,
                    showTimeLoss = true,
                    timeLossAmount = penalty
                )
            }
            triggerTimeLossFeedback()
        }

        if (isGameOver) {
            _events.trySend(GameUiEvent.VibrateWarning)
            handleGameOver()
        } else {
            scope.launch {
                delay(if (isResuming) 500 else 1000)
                if (!_state.value.game.isGameOver) {
                    _events.send(GameUiEvent.PlayFlip)
                    val resetState = resetErrorCardsUseCase(newState)
                    _state.update { it.copy(game = resetState) }
                    saveGame()
                }
            }
        }
    }

    private fun triggerTimeLossFeedback() {
        timeLossJob?.cancel()
        timeLossJob = scope.launch {
            delay(1500)
            _state.update { it.copy(showTimeLoss = false) }
        }
    }

    private fun handleGameWon(newState: MemoryGameState) {
        stopTimer()

        val gameWithBonuses = calculateFinalScoreUseCase(newState, _state.value.elapsedTimeSeconds)
        val isNewHigh = gameWithBonuses.score > _state.value.bestScore

        scope.launch {
            _events.send(GameUiEvent.VibrateMatch)
            if (isNewHigh) {
                _events.send(GameUiEvent.PlayHighScore)
            } else {
                _events.send(GameUiEvent.PlayWin)
            }
        }

        _state.update { it.copy(
            game = gameWithBonuses,
            isNewHighScore = isNewHigh
        ) }

        saveStats(gameWithBonuses.pairCount, gameWithBonuses.score, _state.value.elapsedTimeSeconds, gameWithBonuses.moves, gameWithBonuses.mode)
        clearCommentAfterDelay()

        scope.launch {
            clearSavedGameUseCase()
        }
    }

    private fun handleGameOver() {
        stopTimer()
        _state.update { it.copy(game = it.game.copy(isGameOver = true)) }
        scope.launch {
            _events.send(GameUiEvent.PlayLose)
            clearSavedGameUseCase()
        }
    }

    private fun triggerComboExplosion() {
        explosionJob?.cancel()
        explosionJob = scope.launch {
            _state.update { it.copy(showComboExplosion = true) }
            delay(1000)
            _state.update { it.copy(showComboExplosion = false) }
        }
    }

    private fun clearCommentAfterDelay() {
        commentJob?.cancel()
        commentJob = scope.launch {
            delay(2500)
            _state.update { it.copy(game = it.game.copy(matchComment = null)) }
        }
    }

    private fun saveStats(pairCount: Int, score: Int, time: Long, moves: Int, gameMode: GameMode) {
        scope.launch {
            saveGameResultUseCase(pairCount, score, time, moves, gameMode)
        }
    }

    private fun saveGame() {
        val currentState = _state.value
        if (!currentState.game.isGameOver && currentState.game.cards.isNotEmpty()) {
            scope.launch {
                saveGameStateUseCase(currentState.game, currentState.elapsedTimeSeconds)
            }
        }
    }

    override fun onRestart() {
        startGame(pairCount, true, mode)
    }

    override fun onBack() {
        onBackClicked()
    }

    override fun onToggleAudio() {
        scope.launch {
            val shouldEnable = !(_state.value.isMusicEnabled || _state.value.isSoundEnabled)
            settingsRepository.setMusicEnabled(shouldEnable)
            settingsRepository.setSoundEnabled(shouldEnable)
        }
    }

    override fun onNextWalkthroughStep() {
        _state.update { it.copy(walkthroughStep = it.walkthroughStep + 1) }
    }

    override fun onCompleteWalkthrough() {
        scope.launch {
            _state.update { it.copy(showWalkthrough = false) }
            settingsRepository.setWalkthroughCompleted(true)
            val isPeekEnabled = settingsRepository.isPeekEnabled.first()
            if (isPeekEnabled) {
                peekCards(_state.value.game.mode)
            } else {
                startTimer(_state.value.game.mode)
            }
        }
    }
}
