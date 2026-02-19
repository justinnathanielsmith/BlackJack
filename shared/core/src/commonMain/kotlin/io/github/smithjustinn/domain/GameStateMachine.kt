package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.services.MatchEvaluator
import io.github.smithjustinn.domain.services.MutatorEngine
import io.github.smithjustinn.domain.services.ScoreKeeper
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val onSaveState: (MemoryGameState, Long) -> Unit,
    private val isResumed: Boolean = false,
) {
    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<MemoryGameState> = _state.asStateFlow()

    private var internalTimeSeconds: Long = initialTimeSeconds

    private val _effects = MutableSharedFlow<GameEffect>(extraBufferCapacity = 64)
    val effects: SharedFlow<GameEffect> = _effects.asSharedFlow()

    private val gameTimer = GameTimer(scope, dispatchers) { dispatch(GameAction.Tick) }
    private var peekJob: Job? = null
    private val mutex = Mutex()
    private val syncManager = GameStateSyncManager(scope, dispatchers, onSaveState)

    init {
        require(initialTimeSeconds >= 0) { "Initial time cannot be negative" }

        // Only trigger initial save for new games to avoid redundant I/O on resume
        if (!isResumed) {
            syncManager.sync(initialState, internalTimeSeconds, GameStateSyncManager.Priority.HIGH)
        }

        // If we are resuming a game with mismatched or too many flipped cards, we need to schedule their reset.
        // Also clear lastMatchedIds to avoid showing stale match animations on resume.
        val cards = initialState.cards
        val faceUpUnmatched = cards.count { it.isFaceUp && !it.isMatched }
        val hasError = cards.any { it.isError }

        if (hasError || faceUpUnmatched >= 2 || initialState.lastMatchedIds.isNotEmpty()) {
            val updatedState = initialState.copy(lastMatchedIds = persistentListOf())
            updateState(updatedState)

            if (hasError || faceUpUnmatched >= 2) {
                scope.launch(dispatchers.default) {
                    val delayMs =
                        if (initialState.activeMutators.contains(DailyChallengeMutator.BLACKOUT)) {
                            MISMATCH_DELAY_MS / 2
                        } else {
                            MISMATCH_DELAY_MS
                        }
                    delay(delayMs)
                    dispatch(GameAction.ProcessMismatch)
                }
            }
        }
    }

    fun dispatch(action: GameAction) {
        scope.launch(dispatchers.default) {
            mutex.withLock {
                if (_state.value.isGameOver && action !is GameAction.Restart) return@withLock

                when (action) {
                    is GameAction.StartGame -> {
                        if (action.gameState != null) updateState(action.gameState)
                        startTimer()
                    }
                    is GameAction.FlipCard -> handleFlipCard(action)
                    is GameAction.DoubleDown -> handleDoubleDown()
                    is GameAction.ProcessMismatch -> handleProcessMismatch()
                    is GameAction.ScanCards -> handleScanCards(action)
                    is GameAction.AddTime -> handleAddTime(action)
                    is GameAction.Tick -> handleTick()
                    is GameAction.Restart -> { /* Handled by UI */ }
                    is GameAction.ClearComment -> updateState(_state.value.copy(matchComment = null))
                }
            }
        }
    }

    private fun applyResult(result: StateMachineResult) {
        updateState(result.state)
        internalTimeSeconds = result.time
        result.effects.forEach { emitEffect(it) }
    }

    @Suppress("CyclomaticComplexMethod")
    private fun handleFlipCard(action: GameAction.FlipCard) {
        val currentState = _state.value
        if (currentState.isGameOver || currentState.cards.count { it.isFaceUp && !it.isMatched } >= 2) return

        val (flippedState, event) = MatchEvaluator.flipCard(currentState, action.cardId)
        if (flippedState === currentState && event == null) return

        val result =
            gameStateMachine(flippedState, internalTimeSeconds) {
                when (event) {
                    GameDomainEvent.CardFlipped -> effect(GameEffect.PlayFlipSound)
                    GameDomainEvent.MatchSuccess, GameDomainEvent.TheNutsAchieved ->
                        handleMatchEvent(
                            flippedState,
                            event,
                        )
                    GameDomainEvent.MatchFailure -> handleMismatchEvent(flippedState)
                    GameDomainEvent.GameWon -> handleGameWon(flippedState)
                    GameDomainEvent.GameOver -> handleGameOver()
                    GameDomainEvent.HeatShieldUsed -> handleHeatShieldUsed(flippedState)
                    null -> {}
                }
            }

        applyResult(result)
    }

    private fun StateMachineBuilder.handleGameWon(flippedState: MemoryGameState) {
        +GameEffect.PlayFlipSound
        +GameEffect.VibrateMatch
        +GameEffect.PlayMatchSound
        stopTimer()
        val finalState = ScoreKeeper.applyFinalBonuses(flippedState, internalTimeSeconds)
        transition { finalState }
        +GameEffect.EarnCurrency(finalState.scoreBreakdown.earnedCurrency.toLong())
        +GameEffect.PlayWinSound
        +GameEffect.VibrateMatch
        +GameEffect.GameWon(finalState)
    }

    private fun StateMachineBuilder.handleGameOver() {
        +GameEffect.PlayFlipSound
        stopTimer()
        +GameEffect.PlayLoseSound
        +GameEffect.GameOver
    }

    private fun StateMachineBuilder.handleMatchEvent(
        flippedState: MemoryGameState,
        event: GameDomainEvent,
    ) {
        +GameEffect.PlayFlipSound
        +GameEffect.VibrateMatch
        +GameEffect.PlayMatchSound

        if (event == GameDomainEvent.TheNutsAchieved) {
            +GameEffect.PlayTheNutsSound
        } else if (flippedState.comboMultiplier >= flippedState.config.heatModeThreshold) {
            +GameEffect.VibrateHeat
        }

        if (flippedState.mode == GameMode.TIME_ATTACK) {
            val bonus = TimeAttackLogic.calculateTimeGain(flippedState.comboMultiplier, flippedState.config)
            updateTime { it + bonus }
            +GameEffect.TimerUpdate(internalTimeSeconds + bonus)
            +GameEffect.TimeGain(bonus)
        }
        transition { MutatorEngine.applyMutators(it) }
    }

    private fun StateMachineBuilder.handleMismatchEvent(flippedState: MemoryGameState) {
        +GameEffect.PlayFlipSound
        +GameEffect.PlayMismatch
        +GameEffect.VibrateMismatch

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
        transition { MutatorEngine.applyMutators(it) }
    }

    private fun StateMachineBuilder.handleHeatShieldUsed(flippedState: MemoryGameState) {
        +GameEffect.PlayFlipSound
        +GameEffect.HeatShieldUsed
        // Does not reset combo or pot.
        // Just updates state which already has shield removed.
        transition { flippedState }
    }

    private fun handleDoubleDown() {
        val result =
            gameStateMachine(_state.value, internalTimeSeconds) {
                val newState = MatchEvaluator.activateDoubleDown(_state.value)
                if (newState != _state.value) {
                    transition { newState }
                    +GameEffect.VibrateHeat
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
                    +GameEffect.TimerUpdate(internalTimeSeconds - penalty)
                    +GameEffect.TimeLoss(penalty.toInt())
                    if (internalTimeSeconds - penalty <= 0L) {
                        stopTimer()
                        +GameEffect.VibrateWarning
                        +GameEffect.PlayLoseSound
                        transition { currentState.copy(isGameOver = true, isGameWon = false, score = 0) }
                        +GameEffect.GameOver
                        return@gameStateMachine
                    }
                }

                transition { MatchEvaluator.resetUnmatchedCards(currentState) }
            }
        applyResult(result)
    }

    private fun handleScanCards(action: GameAction.ScanCards) {
        peekJob?.cancel()
        peekJob =
            scope.launch(dispatchers.default) {
                val currentState = _state.value
                val peekCards =
                    currentState.cards
                        .map {
                            if (!it.isMatched) it.copy(isFaceUp = true) else it
                        }.toPersistentList()
                updateState(currentState.copy(cards = peekCards))

                delay(action.durationMs)

                val latestState = _state.value
                val hiddenCards =
                    latestState.cards
                        .map {
                            if (!it.isMatched) it.copy(isFaceUp = false) else it
                        }.toPersistentList()
                updateState(latestState.copy(cards = hiddenCards))
            }
    }

    private fun handleAddTime(action: GameAction.AddTime) {
        val result =
            gameStateMachine(_state.value, internalTimeSeconds) {
                val newTime = (internalTimeSeconds + action.seconds).coerceAtLeast(0)
                updateTime { newTime }
                +GameEffect.TimerUpdate(newTime)
            }
        applyResult(result)
    }

    private fun handleTick() {
        val result =
            gameStateMachine(_state.value, internalTimeSeconds) {
                val currentState = _state.value
                if (currentState.isGameOver) return@gameStateMachine

                if (currentState.mode == GameMode.TIME_ATTACK) {
                    updateTime { (it - 1).coerceAtLeast(0) }
                    val nextTime = (internalTimeSeconds - 1).coerceAtLeast(0)
                    +GameEffect.TimerUpdate(nextTime)

                    if (nextTime <= 0) {
                        stopTimer()
                        +GameEffect.VibrateWarning
                        +GameEffect.PlayLoseSound
                        transition { currentState.copy(isGameOver = true, isGameWon = false, score = 0) }
                        +GameEffect.GameOver
                    } else if (nextTime <= LOW_TIME_WARNING_THRESHOLD) {
                        +GameEffect.VibrateTick
                    }
                } else {
                    updateTime { it + 1 }
                    +GameEffect.TimerUpdate(internalTimeSeconds + 1)
                }
            }
        applyResult(result)
    }

    fun startTimer() = gameTimer.start()

    fun stopTimer() = gameTimer.stop()

    /**
     * Ensures all pending state changes are saved. Call this when the game session ends.
     */
    suspend fun flush() {
        syncManager.flush(_state.value, internalTimeSeconds)
    }

    private fun updateState(newState: MemoryGameState) {
        val oldMoves = _state.value.moves
        _state.value = newState

        val priority =
            if (newState.isGameOver || newState.moves > oldMoves) {
                GameStateSyncManager.Priority.HIGH
            } else {
                GameStateSyncManager.Priority.NORMAL
            }

        syncManager.sync(newState, internalTimeSeconds, priority)
    }

    private fun emitEffect(effect: GameEffect) = _effects.tryEmit(effect)

    companion object {
        const val MISMATCH_DELAY_MS = 1000L
        private const val LOW_TIME_WARNING_THRESHOLD = 5L
    }
}
