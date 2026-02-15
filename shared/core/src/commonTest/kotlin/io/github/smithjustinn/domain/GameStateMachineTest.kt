package io.github.smithjustinn.domain

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
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

            val savedStates = mutableListOf<MemoryGameState>()
            val machine =
                createStateMachine(
                    initialState = state,
                    onSaveState = { s, _ -> savedStates.add(s) },
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
                assertEquals(0, currentState.score, "Score should be 0 (points in pot)")
                assertTrue(currentState.currentPot > 0, "Pot should be greater than 0")

                // Ensure background saves are processed
                advanceUntilIdle()
                assertTrue(
                    savedStates.contains(currentState),
                    "Saved states should contain the matched state. Found: ${savedStates.map { it.moves }}",
                )
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
                val penalty = machine.state.value.config.timeAttackMismatchPenalty
                assertEquals(GameEffect.TimerUpdate(INITIAL_TIME - penalty), awaitItem())
                val lossEffect = awaitItem()
                assertTrue(lossEffect is GameEffect.TimeLoss, "Expected TimeLoss effect")
                assertEquals(penalty.toInt(), (lossEffect as GameEffect.TimeLoss).amount)
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

    @Test
    fun `Blackout mutator reduces mismatch delay`() =
        runTest {
            val state =
                MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK).copy(
                    activeMutators = setOf(DailyChallengeMutator.BLACKOUT),
                )
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

                // With Blackout, delay should be 500ms (MISMATCH_DELAY_MS / 2)
                advanceTimeBy(501)

                // ProcessMismatch happens
                val timerUpdate = awaitItem()
                assertTrue(timerUpdate is GameEffect.TimerUpdate)
                val timeLoss = awaitItem()
                assertTrue(timeLoss is GameEffect.TimeLoss)
            }
        }

    @Test
    fun `game win triggers EarnCurrency effect`() =
        runTest {
            val state = MemoryGameLogic.createInitialState(pairCount = 1) // Only one pair to win quickly
            val firstCard = state.cards[0]
            val matchingCard = state.cards[1]

            val machine = createStateMachine(initialState = state)

            machine.effects.test {
                machine.dispatch(GameAction.FlipCard(firstCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())

                machine.dispatch(GameAction.FlipCard(matchingCard.id))
                // Match effects
                assertEquals(GameEffect.PlayFlipSound, awaitItem())
                assertEquals(GameEffect.VibrateMatch, awaitItem())
                assertEquals(GameEffect.PlayMatchSound, awaitItem())
                // EarnCurrency effect is emitted as part of game won processing
                val earnEffect = awaitItem()
                assertTrue(earnEffect is GameEffect.EarnCurrency, "Expected EarnCurrency effect")
                // Win effects
                assertEquals(GameEffect.PlayWinSound, awaitItem())
                assertEquals(GameEffect.VibrateMatch, awaitItem())
                val wonEffect = awaitItem()
                assertTrue(wonEffect is GameEffect.GameWon, "Expected GameWon effect")
            }
        }

    @Test
    fun `busting with Double Down should emit GameOver and expected effects`() =
        runTest {
            val state = MemoryGameLogic.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
            // Force Double Down active
            val ddState = state.copy(isDoubleDownActive = true)

            val firstCard = ddState.cards[0]
            val nonMatchCard = ddState.cards.drop(1).first { it.suit != firstCard.suit || it.rank != firstCard.rank }

            val machine = createStateMachine(initialState = ddState)

            machine.effects.test {
                machine.dispatch(GameAction.FlipCard(firstCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())

                machine.dispatch(GameAction.FlipCard(nonMatchCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())
                assertEquals(GameEffect.PlayLoseSound, awaitItem())
                assertEquals(GameEffect.GameOver, awaitItem())
            }

            assertTrue(machine.state.value.isGameOver, "State should be marked as GameOver")
            assertTrue(machine.state.value.isBusted, "State should be marked as Busted")
        }

    @Test
    fun `AddTime action updates timer`() =
        runTest {
            val machine = createStateMachine()
            val bonus = 10

            machine.effects.test {
                machine.dispatch(GameAction.AddTime(bonus))

                val effect = awaitItem()
                assertTrue(effect is GameEffect.TimerUpdate)
                assertEquals(INITIAL_TIME + bonus, effect.seconds)
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
        isResumed = false,
    )
}
