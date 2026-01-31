package io.github.smithjustinn.domain

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GameStateMachineTest {
    private val testDispatchers =
        CoroutineDispatchers(
            main = Dispatchers.Default,
            mainImmediate = Dispatchers.Default,
            io = Dispatchers.Default,
            default = Dispatchers.Default,
        )

    @Test
    fun `initial state is correct`() {
        val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
        val machine =
            GameStateMachine(
                scope = CoroutineScope(Dispatchers.Default),
                testDispatchers,
                initialState = initialState,
                initialTimeSeconds = 60,
                onSaveState = { _, _ -> },
            )

        assertEquals(initialState, machine.state.value)
    }

    @Test
    fun `mismatch emits PlayMismatch and VibrateMismatch effects`() =
        runTest {
            val state = MemoryGameLogic.createInitialState(pairCount = 6)
            val firstCard = state.cards[0]
            val nonMatchCard = state.cards.drop(1).first { it.suit != firstCard.suit || it.rank != firstCard.rank }

            val machine =
                GameStateMachine(
                    scope = this,
                    testDispatchers,
                    initialState = state,
                    initialTimeSeconds = 60,
                    onSaveState = { _, _ -> },
                )

            machine.effects.test {
                machine.dispatch(GameAction.FlipCard(firstCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())

                machine.dispatch(GameAction.FlipCard(nonMatchCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())
                assertEquals(GameEffect.PlayMismatch, awaitItem())
                assertEquals(GameEffect.VibrateMismatch, awaitItem())
            }
        }
}
