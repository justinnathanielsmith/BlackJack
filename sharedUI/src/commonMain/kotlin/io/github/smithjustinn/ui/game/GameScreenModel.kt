package io.github.smithjustinn.ui.game

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.*
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*

@Inject
class GameScreenModel(
    private val startNewGameUseCase: StartNewGameUseCase,
    private val flipCardUseCase: FlipCardUseCase,
    private val resetErrorCardsUseCase: ResetErrorCardsUseCase,
    private val calculateFinalScoreUseCase: CalculateFinalScoreUseCase,
    private val getGameStatsUseCase: GetGameStatsUseCase,
    private val saveGameResultUseCase: SaveGameResultUseCase,
    private val getSavedGameUseCase: GetSavedGameUseCase,
    private val saveGameStateUseCase: SaveGameStateUseCase,
    private val clearSavedGameUseCase: ClearSavedGameUseCase,
    private val settingsRepository: SettingsRepository,
    private val logger: Logger
) : ScreenModel {
    private val _state = MutableStateFlow(GameUIState())
    val state: StateFlow<GameUIState> = _state.asStateFlow()

    private val _events = Channel<GameUiEvent>(Channel.BUFFERED)
    val events: Flow<GameUiEvent> = _events.receiveAsFlow()

    private var timerJob: Job? = null
    private var commentJob: Job? = null
    private var statsJob: Job? = null
    private var explosionJob: Job? = null
    private var peekJob: Job? = null
    private var timeGainJob: Job? = null
    private var timeLossJob: Job? = null
    private var settingsJob: Job? = null

    init {
        settingsJob = screenModelScope.launch {
            settingsRepository.isPeekEnabled.collect { peek ->
                _state.update { it.copy(isPeekFeatureEnabled = peek) }
            }
        }
    }

    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartGame -> startGame(intent.pairCount, intent.forceNewGame, intent.mode)
            is GameIntent.FlipCard -> flipCard(intent.cardId)
            is GameIntent.SaveGame -> saveGame()
        }
    }

    private fun startGame(pairCount: Int, forceNewGame: Boolean, mode: GameMode) {
        screenModelScope.launch {
            try {
                val savedGame = if (forceNewGame) null else getSavedGameUseCase()
                if (savedGame != null && savedGame.first.pairCount == pairCount && !savedGame.first.isGameOver && savedGame.first.mode == mode) {
                    val initialTime = if (mode == GameMode.TIME_ATTACK) MemoryGameLogic.calculateInitialTime(pairCount) else 0L
                    _state.update {
                        it.copy(
                            game = savedGame.first.copy(lastMatchedIds = emptyList()), // Clear last matched IDs on resume to prevent stuck animation
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

                    // If the game was saved with mismatched cards flipped, trigger the reset
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

                    // Wait for the settings to be loaded if they haven't been yet
                    val isPeekEnabled = settingsRepository.isPeekEnabled.first()
                    if (isPeekEnabled) {
                        peekCards(mode)
                    } else {
                        startTimer(mode)
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
        peekJob = screenModelScope.launch {
            stopTimer()
            val peekDuration = 3
            _state.update { it.copy(isPeeking = true, peekCountdown = peekDuration) }
            
            for (i in peekDuration downTo 1) {
                _state.update { it.copy(peekCountdown = i) }
                delay(1000)
            }
            
            _state.update { it.copy(isPeeking = false, peekCountdown = 0) }
            _events.send(GameUiEvent.PlayFlip)
            startTimer(mode)
        }
    }

    private fun observeStats(pairCount: Int) {
        statsJob?.cancel()
        statsJob = screenModelScope.launch {
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
        timerJob = screenModelScope.launch {
            while (true) {
                delay(1000)
                if (mode == GameMode.TIME_ATTACK) {
                    var shouldStop = false
                    _state.update { 
                        val newTime = (it.elapsedTimeSeconds - 1).coerceAtLeast(0)
                        if (newTime == 0L && !it.game.isGameOver) {
                            shouldStop = true
                        }
                        it.copy(elapsedTimeSeconds = newTime)
                    }
                    if (shouldStop) {
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

    private fun flipCard(cardId: Int) {
        if (_state.value.isPeeking || _state.value.game.isGameOver) return

        try {
            val (newState, event) = flipCardUseCase(_state.value.game, cardId)

            _state.update { it.copy(game = newState) }
            saveGame()

            when (event) {
                GameDomainEvent.CardFlipped -> screenModelScope.launch { _events.send(GameUiEvent.PlayFlip) }
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
        screenModelScope.launch {
            _events.send(GameUiEvent.VibrateMatch)
            _events.send(GameUiEvent.PlayMatch)
        }
        clearCommentAfterDelay()
        
        if (newState.mode == GameMode.TIME_ATTACK) {
            // Use the multiplier from the match just made (newState.comboMultiplier - 1)
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
        timeGainJob = screenModelScope.launch {
            delay(1500)
            _state.update { it.copy(showTimeGain = false) }
        }
    }

    private fun handleMatchFailure(newState: MemoryGameState, isResuming: Boolean = false) {
        if (!isResuming) {
            screenModelScope.launch {
                _events.send(GameUiEvent.VibrateMismatch)
                _events.send(GameUiEvent.PlayMismatch)
            }
        }
        
        var isGameOver = false
        // Only apply penalty if NOT resuming, as it should have been applied when the mismatch occurred
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
            handleGameOver()
        } else {
            screenModelScope.launch {
                // Use a shorter delay if resuming to clear the "stuck" state faster
                delay(if (isResuming) 500 else 1000)
                // Re-check game over state before resetting cards to avoid race conditions
                if (!_state.value.game.isGameOver) {
                    _events.send(GameUiEvent.PlayFlip)
                    val resetState = resetErrorCardsUseCase(newState)
                    _state.update { it.copy(game = resetState) }
                    saveGame() // Ensure the reset state is saved
                }
            }
        }
    }

    private fun triggerTimeLossFeedback() {
        timeLossJob?.cancel()
        timeLossJob = screenModelScope.launch {
            delay(1500)
            _state.update { it.copy(showTimeLoss = false) }
        }
    }

    private fun handleGameWon(newState: MemoryGameState) {
        screenModelScope.launch {
            _events.send(GameUiEvent.VibrateMatch)
            _events.send(GameUiEvent.PlayWin)
        }
        stopTimer()

        val gameWithBonuses = calculateFinalScoreUseCase(newState, _state.value.elapsedTimeSeconds)
        val isNewHigh = gameWithBonuses.score > _state.value.bestScore

        _state.update { it.copy(
            game = gameWithBonuses,
            isNewHighScore = isNewHigh
        ) }

        saveStats(gameWithBonuses.pairCount, gameWithBonuses.score, _state.value.elapsedTimeSeconds, gameWithBonuses.moves, gameWithBonuses.mode)
        clearCommentAfterDelay()

        screenModelScope.launch {
            clearSavedGameUseCase()
        }
    }

    private fun handleGameOver() {
        stopTimer()
        _state.update { it.copy(game = it.game.copy(isGameOver = true)) }
        screenModelScope.launch {
            clearSavedGameUseCase()
        }
    }

    private fun triggerComboExplosion() {
        explosionJob?.cancel()
        explosionJob = screenModelScope.launch {
            _state.update { it.copy(showComboExplosion = true) }
            delay(1000)
            _state.update { it.copy(showComboExplosion = false) }
        }
    }

    private fun clearCommentAfterDelay() {
        commentJob?.cancel()
        commentJob = screenModelScope.launch {
            delay(2500)
            _state.update { it.copy(game = it.game.copy(matchComment = null)) }
        }
    }

    private fun saveStats(pairCount: Int, score: Int, time: Long, moves: Int, gameMode: GameMode) {
        screenModelScope.launch {
            saveGameResultUseCase(pairCount, score, time, moves, gameMode)
        }
    }

    private fun saveGame() {
        val currentState = _state.value
        if (!currentState.game.isGameOver) {
            screenModelScope.launch {
                saveGameStateUseCase(currentState.game, currentState.elapsedTimeSeconds)
            }
        }
    }

    override fun onDispose() {
        stopTimer()
        commentJob?.cancel()
        statsJob?.cancel()
        explosionJob?.cancel()
        peekJob?.cancel()
        timeGainJob?.cancel()
        timeLossJob?.cancel()
        settingsJob?.cancel()
        saveGame()
    }
}
