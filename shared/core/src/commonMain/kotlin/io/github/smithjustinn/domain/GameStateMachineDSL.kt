package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState

@DslMarker
annotation class GameDsl

@GameDsl
class StateMachineBuilder(
    private var currentState: MemoryGameState,
    private var currentTime: Long,
) {
    private val effects = mutableListOf<GameEffect>()
    private var nextState: MemoryGameState = currentState
    private var nextTime: Long = currentTime

    fun transition(block: (MemoryGameState) -> MemoryGameState) {
        nextState = block(nextState)
    }

    fun updateTime(block: (Long) -> Long) {
        nextTime = block(nextTime)
    }

    fun effect(effect: GameEffect) {
        effects.add(effect)
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
