package io.github.smithjustinn.ui.game

import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.TimeAttackLogic
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

internal object GameConstants {
    const val DOUBLE_DOWN_DURATION = 1
    const val MILLIS_IN_DAY = 86400000L
    const val SETTINGS_COLLECTION_DELAY = 50L
    const val PEEK_DURATION_SECONDS = 3
    const val TIMER_TICK_MS = 1000L
    const val UI_FEEDBACK_DURATION_MS = 1500L
    const val RESUME_DELAY_MS = 500L
    const val REVEAL_DELAY_MS = 1000L
    const val COMBO_EXPLOSION_DURATION_MS = 1000L
    const val COMMENT_DURATION_MS = 2500L
    const val MIN_TICK_TIME = 1L
    const val MAX_TICK_TIME = 5L
    const val MEGA_BONUS_THRESHOLD = 3
}

internal class GameTimerHandler(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<GameUIState>,
    private val events: MutableSharedFlow<GameUiEvent>,
    private val onGameOver: () -> Unit,
) {
    private var timerJob: Job? = null
    private var peekJob: Job? = null

    fun startTimer(mode: GameMode) {
        timerJob?.cancel()
        timerJob =
            scope.launch {
                while (isActive) {
                    delay(GameConstants.TIMER_TICK_MS)
                    if (mode == GameMode.TIME_ATTACK) {
                        var shouldStop = false
                        state.update {
                            val newTime = (it.elapsedTimeSeconds - 1).coerceAtLeast(0)
                            if (newTime == 0L && !it.game.isGameOver) {
                                shouldStop = true
                            }

                            if (newTime in GameConstants.MIN_TICK_TIME..GameConstants.MAX_TICK_TIME &&
                                !it.game.isGameOver
                            ) {
                                events.tryEmit(GameUiEvent.VibrateTick)
                            }

                            it.copy(elapsedTimeSeconds = newTime)
                        }
                        if (shouldStop) {
                            events.emit(GameUiEvent.VibrateWarning)
                            onGameOver()
                            break
                        }
                    } else {
                        state.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
                    }
                }
            }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    fun peekCards(
        mode: GameMode,
        peekDuration: Int = GameConstants.PEEK_DURATION_SECONDS,
    ) {
        peekJob?.cancel()
        peekJob =
            scope.launch {
                stopTimer()
                state.update { it.copy(isPeeking = true, peekCountdown = peekDuration) }

                for (i in peekDuration downTo 1) {
                    state.update { it.copy(peekCountdown = i) }
                    events.emit(GameUiEvent.VibrateTick)
                    delay(GameConstants.TIMER_TICK_MS)
                }

                state.update { it.copy(isPeeking = false, peekCountdown = 0) }
                events.emit(GameUiEvent.PlayFlip)
                startTimer(mode)
            }
    }
}

internal class GameFeedbackHandler(
    private val scope: CoroutineScope,
    private val state: MutableStateFlow<GameUIState>,
    private val events: MutableSharedFlow<GameUiEvent>,
    private val onGameOver: () -> Unit,
    private val onResetCards: suspend (MemoryGameState) -> Unit,
) {
    private var commentJob: Job? = null
    private var explosionJob: Job? = null
    private var timeGainJob: Job? = null
    private var timeLossJob: Job? = null

    fun handleMatchSuccess(
        newState: MemoryGameState,
        isHeatMode: Boolean,
        isNowInHeatMode: Boolean,
    ) {
        events.tryEmit(GameUiEvent.VibrateMatch)
        events.tryEmit(GameUiEvent.PlayMatch)

        clearCommentAfterDelay()

        if (isNowInHeatMode && !isHeatMode) {
            events.tryEmit(GameUiEvent.VibrateHeat)
        }

        if (newState.mode == GameMode.TIME_ATTACK) {
            val totalTimeGain = TimeAttackLogic.calculateTimeGain(newState.comboMultiplier - 1)
            val isMega = newState.comboMultiplier >= GameConstants.MEGA_BONUS_THRESHOLD

            state.update {
                it.copy(
                    elapsedTimeSeconds = it.elapsedTimeSeconds + totalTimeGain,
                    showTimeGain = true,
                    timeGainAmount = totalTimeGain,
                    isMegaBonus = isMega,
                    isHeatMode = isNowInHeatMode,
                )
            }
            triggerTimeGainFeedback()
        } else {
            state.update { it.copy(isHeatMode = isNowInHeatMode) }
        }

        if (newState.comboMultiplier > 2) {
            triggerComboExplosion()
        }
    }

    fun handleMatchFailure(
        newState: MemoryGameState,
        isResuming: Boolean,
    ) {
        if (!isResuming) {
            events.tryEmit(GameUiEvent.VibrateMismatch)
            events.tryEmit(GameUiEvent.PlayMismatch)
        }

        state.update { it.copy(isHeatMode = false) }

        var isGameOver = false
        if (newState.mode == GameMode.TIME_ATTACK && !isResuming) {
            val penalty = TimeAttackLogic.TIME_PENALTY_MISMATCH
            state.update {
                val newTime = (it.elapsedTimeSeconds - penalty).coerceAtLeast(0)
                if (newTime == 0L) isGameOver = true
                it.copy(
                    elapsedTimeSeconds = newTime,
                    showTimeLoss = true,
                    timeLossAmount = penalty,
                )
            }
            triggerTimeLossFeedback()
        }

        if (isGameOver) {
            events.tryEmit(GameUiEvent.VibrateWarning)
            onGameOver()
        } else {
            scope.launch {
                delay(if (isResuming) GameConstants.RESUME_DELAY_MS else GameConstants.REVEAL_DELAY_MS)
                if (!state.value.game.isGameOver) {
                    events.tryEmit(GameUiEvent.PlayFlip)
                    onResetCards(newState)
                }
            }
        }
    }

    private fun triggerTimeGainFeedback() {
        timeGainJob?.cancel()
        timeGainJob =
            scope.launch {
                delay(GameConstants.UI_FEEDBACK_DURATION_MS)
                state.update { it.copy(showTimeGain = false) }
            }
    }

    private fun triggerTimeLossFeedback() {
        timeLossJob?.cancel()
        timeLossJob =
            scope.launch {
                delay(GameConstants.UI_FEEDBACK_DURATION_MS)
                state.update { it.copy(showTimeLoss = false) }
            }
    }

    private fun triggerComboExplosion() {
        explosionJob?.cancel()
        explosionJob =
            scope.launch {
                state.update { it.copy(showComboExplosion = true) }
                delay(GameConstants.COMBO_EXPLOSION_DURATION_MS)
                state.update { it.copy(showComboExplosion = false) }
            }
    }

    fun clearCommentAfterDelay() {
        commentJob?.cancel()
        commentJob =
            scope.launch {
                delay(GameConstants.COMMENT_DURATION_MS)
                state.update { it.copy(game = it.game.copy(matchComment = null)) }
            }
    }
}

internal class GameLifecycleHandler(
    private val appGraph: AppGraph,
    private val state: MutableStateFlow<GameUIState>,
    private val events: MutableSharedFlow<GameUiEvent>,
    private val timerHandler: GameTimerHandler,
    private val onMatchFailure: (MemoryGameState, Boolean) -> Unit,
) {
    fun resumeExistingGame(savedGame: Pair<MemoryGameState, Long>) {
        val initialTime =
            if (savedGame.first.mode == GameMode.TIME_ATTACK) {
                TimeAttackLogic.calculateInitialTime(savedGame.first.pairCount)
            } else {
                0L
            }

        state.update {
            it.copy(
                game = savedGame.first.copy(lastMatchedIds = persistentListOf()),
                elapsedTimeSeconds = savedGame.second,
                maxTimeSeconds = initialTime,
                showComboExplosion = false,
                isNewHighScore = false,
                isPeeking = false,
                showTimeGain = false,
                showTimeLoss = false,
                hasUsedDoubleDownPeek = false,
                isHeatMode = savedGame.first.comboMultiplier >= savedGame.first.config.heatModeThreshold,
            )
        }
        timerHandler.startTimer(savedGame.first.mode)

        if (savedGame.first.cards.any { it.isError }) {
            onMatchFailure(savedGame.first, true)
        }
    }

    fun setupNewGameState(
        initialGameState: MemoryGameState,
        mode: GameMode,
        pairCount: Int,
    ) {
        val initialTime =
            if (mode == GameMode.TIME_ATTACK) {
                TimeAttackLogic.calculateInitialTime(pairCount)
            } else {
                0L
            }

        state.update {
            it.copy(
                game = initialGameState,
                elapsedTimeSeconds = initialTime,
                maxTimeSeconds = initialTime,
                showComboExplosion = false,
                isNewHighScore = false,
                isPeeking = false,
                showTimeGain = false,
                showTimeLoss = false,
                hasUsedDoubleDownPeek = false,
            )
        }

        events.tryEmit(GameUiEvent.PlayDeal)
    }

    fun isSavedGameValid(
        savedGame: Pair<MemoryGameState, Long>,
        pairCount: Int,
        mode: GameMode,
    ): Boolean =
        savedGame.first.pairCount == pairCount &&
            !savedGame.first.isGameOver &&
            savedGame.first.mode == mode

    fun saveGame() {
        appGraph.applicationScope.launch(appGraph.coroutineDispatchers.io) {
            val currentState = state.value
            if (!currentState.game.isGameOver && currentState.game.cards.isNotEmpty()) {
                appGraph.saveGameStateUseCase(currentState.game, currentState.elapsedTimeSeconds)
            }
        }
    }
}
