package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.usecases.economy.EarnCurrencyUseCase
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Encapsulates the state transitions and side effects of the Memory Game.
 * This effectively replaces the scattered logic in `GameComponent` handlers.
 */
@Suppress("TooManyFunctions") // State machine with dedicated handlers for each action
class GameStateMachine(
    private val scope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val initialState: MemoryGameState,
    private val initialTimeSeconds: Long,
    private val earnCurrencyUseCase: EarnCurrencyUseCase,
    private val onSaveState: (MemoryGameState, Long) -> Unit,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<MemoryGameState> = _state.asStateFlow()

    private var internalTimeSeconds: Long = initialTimeSeconds

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private val gameTimer = GameTimer(scope, dispatchers) { dispatch(GameAction.Tick) }
    private var peekJob: kotlinx.coroutines.Job? = null
    private var saveJob: kotlinx.coroutines.Job? = null
    private val mutex = Mutex()

    init {
        // Initial save
        onSaveState(initialState, internalTimeSeconds)
    }

    fun dispatch(action: GameAction) {
        scope.launch(dispatchers.default) {
            mutex.withLock {
                if (_state.value.isGameOver && action !is GameAction.Restart) return@withLock

                when (action) {
                    is GameAction.StartGame -> {
                        if (action.gameState != null) _state.update { action.gameState }
                        startTimer()
                    }
                    is GameAction.FlipCard -> handleFlipCard(action)
                    is GameAction.DoubleDown -> handleDoubleDown()
                    is GameAction.ProcessMismatch -> handleProcessMismatch()
                    is GameAction.ScanCards -> handleScanCards(action)
                    is GameAction.Tick -> handleTick()
                    is GameAction.Restart -> { /* Handled by UI */ }
                    is GameAction.ClearComment -> _state.update { it.copy(matchComment = null) }
                }
            }
        }
    }

    private fun applyResult(result: StateMachineResult) {
        updateState(result.state)
        internalTimeSeconds = result.time
        result.effects.forEach { emitEffect(it) }
    }

    @Suppress("CyclomaticComplexMethod") // Dispatches effects based on game domain events
    private fun handleFlipCard(action: GameAction.FlipCard) {
        val currentState = _state.value
        val faceUpUnmatched = currentState.cards.filter { it.isFaceUp && !it.isMatched }

        // Guard: Skip if game over or 2 cards already face up
        if (currentState.isGameOver || faceUpUnmatched.size >= 2) return

        val (flippedState, event) = MemoryGameLogic.flipCard(currentState, action.cardId)
        if (flippedState === currentState && event == null) return

        val result =
            gameStateMachine(flippedState, internalTimeSeconds) {
                when (event) {
                    GameDomainEvent.CardFlipped -> effect(GameEffect.PlayFlipSound)

                    GameDomainEvent.MatchSuccess, GameDomainEvent.TheNutsAchieved ->
                        handleMatchEvent(flippedState, event)

                    GameDomainEvent.MatchFailure ->
                        handleMismatchEvent(flippedState)

                    GameDomainEvent.GameWon -> {
                        effect(GameEffect.PlayFlipSound)
                        stopTimer()
                        val finalState = MemoryGameLogic.applyFinalBonuses(flippedState, internalTimeSeconds)
                        transition { finalState }
                        scope.launch {
                            earnCurrencyUseCase.execute(finalState.scoreBreakdown.earnedCurrency.toLong())
                        }
                        effect(GameEffect.PlayWinSound)
                        effect(GameEffect.VibrateMatch)
                        effect(GameEffect.GameWon(finalState))
                    }

                    GameDomainEvent.GameOver -> {
                        stopTimer()
                        effect(GameEffect.PlayLoseSound)
                    }

                    null -> {}
                }
            }

        applyResult(result)
    }

    private fun StateMachineBuilder.handleMatchEvent(
        flippedState: MemoryGameState,
        event: GameDomainEvent,
    ) {
        effect(GameEffect.PlayFlipSound)
        effect(GameEffect.VibrateMatch)
        effect(GameEffect.PlayMatchSound)
        if (event == GameDomainEvent.TheNutsAchieved) {
            effect(GameEffect.PlayTheNutsSound)
        } else if (flippedState.comboMultiplier >= flippedState.config.heatModeThreshold) {
            effect(GameEffect.VibrateHeat)
        }
        if (flippedState.mode == GameMode.TIME_ATTACK) {
            val bonus =
                TimeAttackLogic.calculateTimeGain(
                    flippedState.comboMultiplier,
                    flippedState.config,
                )
            updateTime { it + bonus }
            effect(GameEffect.TimerUpdate(internalTimeSeconds + bonus))
            effect(GameEffect.TimeGain(bonus))
        }
        // Apply mutators (e.g. Mirage swap)
        transition { MemoryGameLogic.applyMutators(it) }
    }

    private fun StateMachineBuilder.handleMismatchEvent(flippedState: MemoryGameState) {
        effect(GameEffect.PlayFlipSound)
        effect(GameEffect.PlayMismatch)
        effect(GameEffect.VibrateMismatch)
        scope.launch(dispatchers.default) {
            val delayMs =
                if (flippedState.activeMutators.contains(DailyChallengeMutator.BLACKOUT)) {
                    MISMATCH_DELAY_MS / 2
                } else {
                    MISMATCH_DELAY_MS
                }
            delay(delayMs)
            dispatch(GameAction.ProcessMismatch)
        }
        // Apply mutators (e.g. Mirage swap)
        transition { MemoryGameLogic.applyMutators(it) }
    }

    private fun handleDoubleDown() {
        val result =
            gameStateMachine(_state.value, internalTimeSeconds) {
                val newState = MemoryGameLogic.activateDoubleDown(_state.value)
                if (newState != _state.value) {
                    transition { newState }
                    effect(GameEffect.VibrateHeat)
                    // Side effect that triggers another action
                    scope.launch { dispatch(GameAction.ScanCards(durationMs = 2000)) }
                }
            }
        applyResult(result)
    }

    private fun handleProcessMismatch() {
        val result =
            gameStateMachine(_state.value, internalTimeSeconds) {
                val currentState = _state.value
                if (currentState.mode == GameMode.TIME_ATTACK) {
                    val penalty = currentState.config.timeAttackMismatchPenalty
                    updateTime { (it - penalty).coerceAtLeast(0) }
                    effect(GameEffect.TimerUpdate(internalTimeSeconds - penalty))
                    effect(GameEffect.TimeLoss(penalty.toInt()))
                    if (internalTimeSeconds - penalty <= 0L) {
                        stopTimer()
                        effect(GameEffect.VibrateWarning)
                        effect(GameEffect.PlayLoseSound)
                        transition { currentState.copy(isGameOver = true, isGameWon = false, score = 0) }
                        effect(GameEffect.GameOver)
                        return@gameStateMachine
                    }
                }

                transition { MemoryGameLogic.resetErrorCards(currentState) }
            }
        applyResult(result)
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
                _state.update { peekState }
                delay(action.durationMs)
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
        val result =
            gameStateMachine(_state.value, internalTimeSeconds) {
                val currentState = _state.value
                if (currentState.isGameOver) return@gameStateMachine

                if (currentState.mode == GameMode.TIME_ATTACK) {
                    updateTime { (it - 1).coerceAtLeast(0) }
                    val nextTime = (internalTimeSeconds - 1).coerceAtLeast(0)
                    effect(GameEffect.TimerUpdate(nextTime))

                    if (nextTime <= 0) {
                        stopTimer()
                        effect(GameEffect.VibrateWarning)
                        effect(GameEffect.PlayLoseSound)
                        transition { currentState.copy(isGameOver = true, isGameWon = false, score = 0) }
                        effect(GameEffect.GameOver)
                    } else if (nextTime <= LOW_TIME_WARNING_THRESHOLD) {
                        effect(GameEffect.VibrateTick)
                    }
                } else {
                    updateTime { it + 1 }
                    effect(GameEffect.TimerUpdate(internalTimeSeconds + 1))
                }
            }
        applyResult(result)
    }

    fun startTimer() = gameTimer.start()

    fun stopTimer() = gameTimer.stop()

    private fun updateState(newState: MemoryGameState) {
        _state.value = newState
        if (newState.isGameOver) {
            saveJob?.cancel()
            onSaveState(newState, internalTimeSeconds)
        } else {
            triggerDebouncedSave()
        }
    }

    private fun triggerDebouncedSave() {
        if (saveJob?.isActive == true) return

        saveJob =
            scope.launch(dispatchers.default) {
                delay(SAVE_DEBOUNCE_DELAY_MS)
                mutex.withLock {
                    onSaveState(_state.value, internalTimeSeconds)
                }
            }
    }

    private fun emitEffect(effect: GameEffect) = _effects.tryEmit(effect)

    companion object {
        const val MISMATCH_DELAY_MS = 1000L
        private const val LOW_TIME_WARNING_THRESHOLD = 5L
        private const val SAVE_DEBOUNCE_DELAY_MS = 2000L
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

    data object ClearComment : GameAction()
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
