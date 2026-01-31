package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Encapsulates the state transitions and side effects of the Memory Game.
 * This effectively replaces the scattered logic in `GameComponent` handlers.
 */
class GameStateMachine(
    private val scope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val initialState: MemoryGameState,
    private val initialTimeSeconds: Long,
    private val timeProvider: () -> Long,
    private val onSaveState: (MemoryGameState, Long) -> Unit,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<MemoryGameState> = _state.asStateFlow()

    private var internalTimeSeconds: Long = initialTimeSeconds

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private var timerJob: Job? = null
    private var peekJob: Job? = null

    init {
        // Initial save
        onSaveState(initialState, internalTimeSeconds)
    }

    fun dispatch(action: GameAction) {
        if (_state.value.isGameOver && action !is GameAction.Restart) return

        when (action) {
            is GameAction.StartGame -> {
                handleStartGame(action)
            }

            is GameAction.FlipCard -> {
                handleFlipCard(action)
            }

            is GameAction.DoubleDown -> {
                handleDoubleDown()
            }

            is GameAction.ProcessMismatch -> {
                handleProcessMismatch()
            }

            is GameAction.ScanCards -> {
                handleScanCards(action)
            }

            is GameAction.Tick -> {
                handleTick()
            }

            is GameAction.Restart -> { /* Handled by UI usually, but we can reset state here if needed */ }
        }
    }

    private fun handleStartGame(action: GameAction.StartGame) {
        if (action.gameState != null) {
            // Resume
            _state.update { action.gameState }
        }
        startTimer()
    }

    private fun handleFlipCard(action: GameAction.FlipCard) {
        val currentState = _state.value
        if (currentState.isGameOver) return

        // Prevent flipping if 2 cards are already face up and unmatched (waiting for mismatch process)
        // Or if peeking
        val faceUpUnmatched = currentState.cards.filter { it.isFaceUp && !it.isMatched }
        if (faceUpUnmatched.size >= 2) return

        val (newState, event) = MemoryGameLogic.flipCard(currentState, action.cardId)

        if (newState === currentState && event == null) return

        updateState(newState)

        when (event) {
            GameDomainEvent.CardFlipped -> {
                emitEffect(GameEffect.PlayFlipSound)
            }

            GameDomainEvent.MatchSuccess -> {
                emitEffect(GameEffect.PlayFlipSound)
                handleMatchSuccess(newState, event)
            }

            GameDomainEvent.TheNutsAchieved -> {
                emitEffect(GameEffect.PlayFlipSound)
                handleMatchSuccess(newState, event)
            }

            GameDomainEvent.MatchFailure -> {
                emitEffect(GameEffect.PlayFlipSound)
                scope.launch(dispatchers.default) {
                    delay(MISMATCH_DELAY_MS)
                    dispatch(GameAction.ProcessMismatch)
                }
            }

            GameDomainEvent.GameWon -> {
                emitEffect(GameEffect.PlayFlipSound)
                stopTimer()

                val finalState = MemoryGameLogic.applyFinalBonuses(newState, internalTimeSeconds)
                updateState(finalState)

                emitEffect(GameEffect.PlayWinSound)
                emitEffect(GameEffect.VibrateMatch)
                emitEffect(GameEffect.GameWon(finalState))
            }

            GameDomainEvent.GameOver -> {
                // e.g. Busted in High Roller
                stopTimer()
                emitEffect(GameEffect.PlayLoseSound)
            }

            null -> {}
        }
    }

    private fun handleMatchSuccess(
        newState: MemoryGameState,
        event: GameDomainEvent,
    ) {
        val isHeatMode = newState.comboMultiplier >= newState.config.heatModeThreshold

        emitEffect(GameEffect.VibrateMatch)
        emitEffect(GameEffect.PlayMatchSound)

        if (event == GameDomainEvent.TheNutsAchieved) {
            emitEffect(GameEffect.PlayTheNutsSound)
        } else if (isHeatMode) {
            emitEffect(GameEffect.VibrateHeat)
        }

        if (newState.mode == GameMode.TIME_ATTACK) {
            val bonus = TimeAttackLogic.calculateTimeGain(newState.comboMultiplier - 1)
            internalTimeSeconds += bonus
            emitEffect(GameEffect.TimerUpdate(internalTimeSeconds))
            emitEffect(GameEffect.TimeGain(bonus.toInt()))
        }
    }

    private fun handleDoubleDown() {
        val newState = MemoryGameLogic.activateDoubleDown(_state.value)
        if (newState != _state.value) {
            updateState(newState)
            emitEffect(GameEffect.VibrateHeat)
            dispatch(GameAction.ScanCards(durationMs = 2000))
        }
    }

    private fun handleProcessMismatch() {
        val currentState = _state.value
        if (currentState.mode == GameMode.TIME_ATTACK) {
            val penalty = TimeAttackLogic.TIME_PENALTY_MISMATCH
            internalTimeSeconds = (internalTimeSeconds - penalty).coerceAtLeast(0)
            emitEffect(GameEffect.TimerUpdate(internalTimeSeconds))
            emitEffect(GameEffect.TimeLoss(penalty.toInt()))
            if (internalTimeSeconds == 0L) {
                handleTimeExpired()
                return
            }
        }

        val newState = MemoryGameLogic.resetErrorCards(currentState)
        updateState(newState)
    }

    private fun handleTimeExpired() {
        stopTimer()
        emitEffect(GameEffect.VibrateWarning)
        emitEffect(GameEffect.PlayLoseSound)
        val newState =
            _state.value.copy(
                isGameOver = true,
                isGameWon = false,
                score = 0,
            )
        updateState(newState)
        emitEffect(GameEffect.GameOver)
    }

    private fun handleScanCards(action: GameAction.ScanCards) {
        peekJob?.cancel()
        peekJob =
            scope.launch(dispatchers.default) {
                val peekState =
                    _state.value.copy(
                        cards =
                            _state.value.cards
                                .map {
                                    if (!it.isMatched) it.copy(isFaceUp = true) else it
                                }.toImmutableList(),
                    )
                // Do not save peek state
                _state.update { peekState }

                delay(action.durationMs)

                // Restore
                _state.update { s ->
                    s.copy(
                        cards =
                            s.cards
                                .map {
                                    if (!it.isMatched) it.copy(isFaceUp = false) else it
                                }.toImmutableList(),
                    )
                }
            }
    }

    private fun handleTick() {
        val currentState = _state.value
        if (currentState.isGameOver) return

        if (currentState.mode == GameMode.TIME_ATTACK) {
            internalTimeSeconds = (internalTimeSeconds - 1).coerceAtLeast(0)
            emitEffect(GameEffect.TimerUpdate(internalTimeSeconds))

            if (internalTimeSeconds <= 0) {
                handleTimeExpired()
            } else if (internalTimeSeconds <= 5) {
                emitEffect(GameEffect.VibrateTick)
            }
        } else {
            internalTimeSeconds++
            emitEffect(GameEffect.TimerUpdate(internalTimeSeconds))
        }
    }

    fun startTimer() {
        stopTimer()
        timerJob =
            scope.launch(dispatchers.default) {
                val startTime = timeProvider()
                while (true) {
                    delay(1000)
                    dispatch(GameAction.Tick)
                }
            }
    }

    fun stopTimer() {
        timerJob?.cancel()
    }

    private fun updateState(newState: MemoryGameState) {
        _state.value = newState
        onSaveState(newState, internalTimeSeconds)
    }

    private fun emitEffect(effect: GameEffect) {
        _effects.tryEmit(effect)
    }

    companion object {
        const val MISMATCH_DELAY_MS = 1000L
    }
}

sealed class GameAction {
    data class StartGame(
        val gameState: MemoryGameState? = null,
    ) : GameAction()

    data class FlipCard(
        val cardId: Int,
    ) : GameAction()

    data object DoubleDown : GameAction()

    data class ScanCards(
        val durationMs: Long,
    ) : GameAction()

    data object ProcessMismatch : GameAction()

    data object Tick : GameAction()

    data object Restart : GameAction()
}

sealed class GameEffect {
    data object PlayFlipSound : GameEffect()

    data object PlayWinSound : GameEffect()

    data object PlayLoseSound : GameEffect()

    data object PlayTheNutsSound : GameEffect()

    data object VibrateMatch : GameEffect()

    data object VibrateHeat : GameEffect()

    data class TimerUpdate(
        val seconds: Long,
    ) : GameEffect()

    data class TimeGain(
        val amount: Int,
    ) : GameEffect()

    data class TimeLoss(
        val amount: Int,
    ) : GameEffect()

    data object PlayMatchSound : GameEffect()

    data object VibrateMismatch : GameEffect()

    data object PlayMismatch : GameEffect()

    data object VibrateWarning : GameEffect()

    data object VibrateTick : GameEffect()

    data object GameOver : GameEffect()

    data class GameWon(
        val finalState: MemoryGameState,
    ) : GameEffect()
}
