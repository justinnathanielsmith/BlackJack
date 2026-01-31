package io.github.smithjustinn.domain

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest : BaseLogicTest() {
    companion object {
        private const val INITIAL_TIME = 60L
        private const val SCAN_CARDS_DELAY_MS = 2000L
        private const val MISMATCH_DELAY_MS = 1000L
    }

    @Test
    fun `initial state is correct`() {
        val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
        val machine = createStateMachine(initialState = initialState)

        assertEquals(initialState, machine.state.value)
    }

    @Test
    fun `mismatch emits PlayMismatch and VibrateMismatch effects`() =
        runTest {
            val state = MemoryGameLogic.createInitialState(pairCount = 6)
            val firstCard = state.cards[0]
            val nonMatchCard =
                state.cards.drop(1).find { it.suit != firstCard.suit || it.rank != firstCard.rank }
                    ?: error("No non-matching card found")

            val machine = createStateMachine(initialState = state)

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
        runTest {
            val state = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
            val firstCard = state.cards[0]
            val matchingCard =
                state.cards.find { it.id != firstCard.id && it.suit == firstCard.suit && it.rank == firstCard.rank }
                    ?: error("No matching card found")

            var savedState: MemoryGameState? = null
            val machine =
                createStateMachine(
                    initialState = state,
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
                assertTrue(timerUpdate is GameEffect.TimerUpdate, "Expected TimerUpdate effect")

                val timeGain = awaitItem()
                assertTrue(timeGain is GameEffect.TimeGain, "Expected TimeGain effect")

                // Check state update
                val currentState = machine.state.value
                assertEquals(1, currentState.cards.count { it.isMatched } / 2)
                assertTrue(currentState.score > 0, "Score should be greater than 0")
                assertEquals(currentState, savedState)
            }
        }

    @Test
    fun `timer ticks in Time Attack mode`() =
        runTest {
            val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
            val machine = createStateMachine(initialState = initialState)

            machine.effects.test {
                machine.dispatch(GameAction.StartGame())

                advanceTimeBy(1001)
                assertEquals(GameEffect.TimerUpdate(INITIAL_TIME - 1), awaitItem())

                advanceTimeBy(1001)
                assertEquals(GameEffect.TimerUpdate(INITIAL_TIME - 2), awaitItem())
            }
        }

    @Test
    fun `mismatch in Time Attack mode triggers penalty`() =
        runTest {
            val state = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
            val firstCard = state.cards[0]
            val nonMatchCard = state.cards.drop(1).first { it.suit != firstCard.suit || it.rank != firstCard.rank }

            val machine = createStateMachine(initialState = state)

            machine.effects.test {
                machine.dispatch(GameAction.FlipCard(firstCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())

                machine.dispatch(GameAction.FlipCard(nonMatchCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())
                assertEquals(GameEffect.PlayMismatch, awaitItem())
                assertEquals(GameEffect.VibrateMismatch, awaitItem())

                // Mismatch penalty delay processing starts
                // We need to wait for MISMATCH_DELAY_MS (1000ms)
                advanceTimeBy(MISMATCH_DELAY_MS + 1)

                // Then ProcessMismatch is dispatched, which emits TimeLoss and TimerUpdate
                assertEquals(GameEffect.TimerUpdate(INITIAL_TIME - TimeAttackLogic.TIME_PENALTY_MISMATCH), awaitItem())
                val lossEffect = awaitItem()
                assertTrue(lossEffect is GameEffect.TimeLoss, "Expected TimeLoss effect")
                assertEquals(TimeAttackLogic.TIME_PENALTY_MISMATCH.toInt(), (lossEffect as GameEffect.TimeLoss).amount)
            }
        }

    @Test
    fun `Double Down requires heat mode and triggers ScanCards`() =
        runTest {
            // Create state with heat mode active (comboMultiplier >= 3) and enough unmatched pairs (>= 3)
            val baseState = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
            val state = baseState.copy(comboMultiplier = 3)

            val machine = createStateMachine(initialState = state)

            machine.effects.test {
                machine.dispatch(GameAction.DoubleDown)

                // VibrateHeat effect from DoubleDown
                assertEquals(GameEffect.VibrateHeat, awaitItem())

                // ScanCards is dispatched via scope.launch
                // ScanCards updates state to peeking (cards face up)
                advanceTimeBy(100)
                assertTrue(
                    machine.state.value.cards
                        .all { it.isFaceUp || it.isMatched },
                    "All cards should be face up or matched during scan",
                )

                // After SCAN_CARDS_DELAY_MS delay, cards should be face down again
                advanceTimeBy(SCAN_CARDS_DELAY_MS + 1)
                assertTrue(
                    machine.state.value.cards
                        .none { it.isFaceUp && !it.isMatched },
                    "All non-matched cards should be face down after scan",
                )
            }
        }

    private fun createStateMachine(
        initialState: MemoryGameState = MemoryGameState(mode = GameMode.TIME_ATTACK),
        initialTimeSeconds: Long = INITIAL_TIME,
        onSaveState: (MemoryGameState, Long) -> Unit = { _, _ -> },
    ) = GameStateMachine(
        scope = testScope,
        dispatchers = testDispatchers,
        initialState = initialState,
        initialTimeSeconds = initialTimeSeconds,
        onSaveState = onSaveState,
    )
}
