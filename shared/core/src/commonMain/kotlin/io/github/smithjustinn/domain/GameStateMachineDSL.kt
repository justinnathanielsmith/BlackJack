package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState

@DslMarker
annotation class GameDsl

@GameDsl
class StateMachineBuilder(
    currentState: MemoryGameState,
    currentTime: Long,
) {
    private val effects = mutableListOf<GameEffect>()
    private var nextState: MemoryGameState = currentState
    private var nextTime: Long = currentTime

    fun transition(block: (MemoryGameState) -> MemoryGameState) {
        nextState = block(nextState)
    }

    fun updateState(block: MemoryGameState.() -> MemoryGameState) {
        nextState = nextState.block()
    }

    fun updateTime(block: (Long) -> Long) {
        nextTime = block(nextTime)
    }

    fun effect(effect: GameEffect) {
        effects.add(effect)
    }

    operator fun GameEffect.unaryPlus() {
        effects.add(this)
    }

    fun build(): StateMachineResult = StateMachineResult(nextState, nextTime, effects)
}

data class StateMachineResult(
    val state: MemoryGameState,
    val time: Long,
    val effects: List<GameEffect>,
)

inline fun gameStateMachine(
    state: MemoryGameState,
    time: Long,
    block: StateMachineBuilder.() -> Unit,
): StateMachineResult = StateMachineBuilder(state, time).apply(block).build()
