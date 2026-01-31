package io.github.smithjustinn.domain

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest {
    private val testDispatcher = StandardTestDispatcher()
    private val testDispatchers =
        CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )

    @Test
    fun `initial state is correct`() {
        val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
        val machine =
            GameStateMachine(
                scope = TestScope(testDispatcher),
                testDispatchers,
                initialState = initialState,
                initialTimeSeconds = 60,
                onSaveState = { _, _ -> },
            )

        assertEquals(initialState, machine.state.value)
    }

    @Test
    fun `mismatch emits PlayMismatch and VibrateMismatch effects`() =
        runTest(testDispatcher) {
            val state = MemoryGameLogic.createInitialState(pairCount = 6)
            val firstCard = state.cards[0]
            val nonMatchCard =
                state.cards.drop(1).find { it.suit != firstCard.suit || it.rank != firstCard.rank }
                    ?: error("No non-matching card found")

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

    @Test
    fun `match success emits corresponding effects and updates score`() =
        runTest(testDispatcher) {
            val state = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
            val firstCard = state.cards[0]
            val matchingCard =
                state.cards.find { it.id != firstCard.id && it.suit == firstCard.suit && it.rank == firstCard.rank }
                    ?: error("No matching card found")

            var savedState: MemoryGameState? = null
            val machine =
                GameStateMachine(
                    scope = this,
                    testDispatchers,
                    initialState = state,
                    initialTimeSeconds = 60,
                    onSaveState = { s, _ -> savedState = s },
                )

            machine.effects.test {
                machine.dispatch(GameAction.FlipCard(firstCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())

                machine.dispatch(GameAction.FlipCard(matchingCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())
                assertEquals(GameEffect.VibrateMatch, awaitItem())
                assertEquals(GameEffect.PlayMatchSound, awaitItem())

                // Assert timer update and time gain in Time Attack
                val timerUpdate = awaitItem()
                assert(timerUpdate is GameEffect.TimerUpdate)

                val timeGain = awaitItem()
                assert(timeGain is GameEffect.TimeGain)

                // Check state update
                val currentState = machine.state.value
                assertEquals(1, currentState.cards.count { it.isMatched } / 2)
                assert(currentState.score > 0)
                assertEquals(currentState, savedState)
            }
        }

    @Test
    fun `timer ticks in Time Attack mode`() =
        runTest(testDispatcher) {
            val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
            val machine =
                GameStateMachine(
                    scope = this,
                    testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 60,
                    onSaveState = { _, _ -> },
                )

            machine.effects.test {
                machine.dispatch(GameAction.StartGame())

                advanceTimeBy(1001)
                assertEquals(GameEffect.TimerUpdate(59), awaitItem())

                advanceTimeBy(1001)
                assertEquals(GameEffect.TimerUpdate(58), awaitItem())
            }
        }

    @Test
    fun `mismatch in Time Attack mode triggers penalty`() =
        runTest(testDispatcher) {
            val state = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
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

                // Mismatch penalty delay processing starts
                // We need to wait for MISMATCH_DELAY_MS (1000ms)
                advanceTimeBy(1001)

                // Then ProcessMismatch is dispatched, which emits TimeLoss and TimerUpdate
                assertEquals(GameEffect.TimerUpdate(60 - TimeAttackLogic.TIME_PENALTY_MISMATCH), awaitItem())
                val lossEffect = awaitItem()
                assert(lossEffect is GameEffect.TimeLoss)
                assertEquals(TimeAttackLogic.TIME_PENALTY_MISMATCH.toInt(), (lossEffect as GameEffect.TimeLoss).amount)
            }
        }

    @Test
    fun `Double Down requires heat mode and triggers ScanCards`() =
        runTest(testDispatcher) {
            // Create state with heat mode active (comboMultiplier >= 3) and enough unmatched pairs (>= 3)
            val baseState = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
            val state = baseState.copy(comboMultiplier = 3)

            val machine =
                GameStateMachine(
                    scope = this,
                    testDispatchers,
                    initialState = state,
                    initialTimeSeconds = 60,
                    onSaveState = { _, _ -> },
                )

            machine.effects.test {
                machine.dispatch(GameAction.DoubleDown)

                // VibrateHeat effect from DoubleDown
                assertEquals(GameEffect.VibrateHeat, awaitItem())

                // ScanCards is dispatched via scope.launch
                // ScanCards updates state to peeking (cards face up)
                advanceTimeBy(100)
                assert(
                    machine.state.value.cards
                        .all { it.isFaceUp || it.isMatched },
                )

                // After 2000ms delay, cards should be face down again
                advanceTimeBy(2001)
                assert(
                    machine.state.value.cards
                        .none { it.isFaceUp && !it.isMatched },
                )
            }
        }
}
