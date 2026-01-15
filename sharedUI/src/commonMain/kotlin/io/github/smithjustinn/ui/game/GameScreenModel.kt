package io.github.smithjustinn.ui.game

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.*
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

/**
 * Represents the overall state of the game screen, including the UI-only state.
 */
data class GameUIState(
    val game: MemoryGameState = MemoryGameState(),
    val elapsedTimeSeconds: Long = 0,
    val maxTimeSeconds: Long = 0,
    val bestScore: Int = 0,
    val bestTimeSeconds: Long = 0,
    val showComboExplosion: Boolean = false,
    val isNewHighScore: Boolean = false,
    val isPeeking: Boolean = false,
    val isPeekFeatureEnabled: Boolean = true,
    val showTimeGain: Boolean = false,
    val timeGainAmount: Int = 0,
    val isMegaBonus: Boolean = false
)

/**
 * Sealed class representing user intents for the game screen.
 */
sealed class GameIntent {
    data class StartGame(
        val pairCount: Int,
        val forceNewGame: Boolean = false,
        val mode: GameMode = GameMode.STANDARD
    ) : GameIntent()
    data class FlipCard(val cardId: Int) : GameIntent()
    data object SaveGame : GameIntent()
}

@Inject
class GameScreenModel(
    private val hapticsService: HapticsService,
    private val audioService: AudioService,
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

    private var timerJob: Job? = null
    private var commentJob: Job? = null
    private var statsJob: Job? = null
    private var explosionJob: Job? = null
    private var peekJob: Job? = null
    private var timeGainJob: Job? = null

    init {
        screenModelScope.launch {
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
                    val initialTime = if (mode == GameMode.TIME_ATTACK) calculateInitialTime(pairCount) else 0L
                    _state.update {
                        it.copy(
                            game = savedGame.first,
                            elapsedTimeSeconds = savedGame.second,
                            maxTimeSeconds = initialTime,
                            showComboExplosion = false,
                            isNewHighScore = false,
                            isPeeking = false,
                            showTimeGain = false
                        )
                    }
                    startTimer(mode)
                } else {
                    val initialGameState = startNewGameUseCase(pairCount, mode = mode)
                    val initialTime = if (mode == GameMode.TIME_ATTACK) calculateInitialTime(pairCount) else 0L
                    
                    _state.update {
                        it.copy(
                            game = initialGameState,
                            elapsedTimeSeconds = initialTime,
                            maxTimeSeconds = initialTime,
                            showComboExplosion = false,
                            isNewHighScore = false,
                            isPeeking = false,
                            showTimeGain = false
                        )
                    }
                    
                    audioService.playDeal() // Play deal sound when a new game starts

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

    private fun calculateInitialTime(pairCount: Int): Long {
        return when (pairCount) {
            6 -> 60L  // Toddler
            8 -> 50L  // Casual
            10 -> 45L // Master
            12 -> 40L // Shark
            14 -> 35L // Grandmaster
            16 -> 30L // Elephant
            else -> (pairCount * 4).toLong()
        }
    }

    private fun peekCards(mode: GameMode) {
        peekJob?.cancel()
        peekJob = screenModelScope.launch {
            stopTimer()
            _state.update { it.copy(isPeeking = true) }
            delay(3000) // Peek for 3 seconds
            _state.update { it.copy(isPeeking = false) }
            audioService.playFlip() // Play flip sound when cards hide after peek
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
        timerJob = screenModelScope.launch(Dispatchers.IO) {
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
                GameDomainEvent.CardFlipped -> audioService.playFlip()
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
        hapticsService.vibrateMatch()
        audioService.playMatch()
        clearCommentAfterDelay()
        
        if (newState.mode == GameMode.TIME_ATTACK) {
            val baseTimeGain = 5
            val comboBonus = (newState.comboMultiplier - 1) * 2
            val totalTimeGain = baseTimeGain + comboBonus
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

    private fun handleMatchFailure(newState: MemoryGameState) {
        hapticsService.vibrateMismatch()
        audioService.playMismatch()
        
        screenModelScope.launch {
            delay(1000)
            audioService.playFlip() // Play flip sound when cards hide after mismatch
            val resetState = resetErrorCardsUseCase(newState)
            _state.update { it.copy(game = resetState) }
        }
    }

    private fun handleGameWon(newState: MemoryGameState) {
        hapticsService.vibrateMatch()
        audioService.playWin()
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
        screenModelScope.launch(Dispatchers.IO) {
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
        saveGame()
    }
}
