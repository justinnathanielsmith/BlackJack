package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.collections.immutable.mutate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest : BaseLogicTest() {
    @Test
    fun `ProcessMismatch in TimeAttack applies penalty and updates time correctly`() =
        runTest {
            val config = ScoringConfig(timeAttackMismatchPenalty = 10L)
            val state =
                GameFactory.createInitialState(pairCount = 4).copy(
                    mode = GameMode.TIME_ATTACK,
                    config = config,
                )

            val machine =
                GameStateMachine(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    initialState = state,
                    initialTimeSeconds = 30L,
                    onSaveState = { _, _ -> },
                    isResumed = true,
                )

            val effects = mutableListOf<GameEffect>()
            val job =
                testScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    machine.effects.collect { effects.add(it) }
                }

            machine.dispatch(GameAction.ProcessMismatch)
            advanceTimeBy(100)
            runCurrent()

            machine.stopTimer()
            job.cancel()
            testScope.coroutineContext[kotlinx.coroutines.Job]?.cancelChildren()

            val timerUpdates = effects.filterIsInstance<GameEffect.TimerUpdate>()
            assertEquals(1, timerUpdates.size)
            assertEquals(20L, timerUpdates.first().seconds)

            val timeLosses = effects.filterIsInstance<GameEffect.TimeLoss>()
            assertEquals(1, timeLosses.size)
            assertEquals(10, timeLosses.first().amount)
        }

    @Test
    fun `ProcessMismatch in TimeAttack triggers GameOver when time reaches zero`() =
        runTest {
            val config = ScoringConfig(timeAttackMismatchPenalty = 10L)
            val state =
                GameFactory.createInitialState(pairCount = 4).copy(
                    mode = GameMode.TIME_ATTACK,
                    config = config,
                    score = 100, // Set a score to verify it gets reset
                )

            val machine =
                GameStateMachine(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    initialState = state,
                    initialTimeSeconds = 5L, // Less than penalty
                    onSaveState = { _, _ -> },
                    isResumed = true,
                )

            val effects = mutableListOf<GameEffect>()
            val job =
                testScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                    machine.effects.collect { effects.add(it) }
                }

            machine.dispatch(GameAction.ProcessMismatch)
            advanceTimeBy(100)
            runCurrent()

            machine.stopTimer()
            job.cancel()
            testScope.coroutineContext[kotlinx.coroutines.Job]?.cancelChildren()

            // TimerUpdate with 0 might not be emitted if internalTimeSeconds
            // was not updated first or we stopped too early.
            // What we *do* know is that it should be Game Over.
            val finalState = machine.state.value

            assertTrue(finalState.isGameOver, "Expected game over to be true")
            assertEquals(false, finalState.isGameWon, "Expected game won to be false")
            assertEquals(0, finalState.score, "Expected score to be 0")

            assertTrue(effects.any { it is GameEffect.TimeLoss && it.amount == 10 }, "Missing TimeLoss(10)")
            assertTrue(effects.any { it is GameEffect.VibrateWarning }, "Missing VibrateWarning")
            assertTrue(effects.any { it is GameEffect.PlayLoseSound }, "Missing PlayLoseSound")
            assertTrue(effects.any { it is GameEffect.GameOver }, "Missing GameOver effect")
        }

    @Test
    fun `ProcessMismatch resets unmatched cards`() =
        runTest {
            val config = ScoringConfig(timeAttackMismatchPenalty = 10L)
            val state =
                GameFactory.createInitialState(pairCount = 4).copy(
                    mode = GameMode.TIME_ATTACK,
                    config = config,
                )

            val faceUpState =
                state.copy(
                    cards =
                        state.cards.mutate { list ->
                            list[0] = list[0].copy(isFaceUp = true, isMatched = false)
                            list[1] = list[1].copy(isFaceUp = true, isMatched = false)
                        },
                )

            val machine =
                GameStateMachine(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    initialState = faceUpState,
                    initialTimeSeconds = 30L,
                    onSaveState = { _, _ -> },
                    isResumed = true,
                )

            machine.dispatch(GameAction.ProcessMismatch)
            advanceTimeBy(100)
            runCurrent()

            machine.stopTimer()
            testScope.coroutineContext[kotlinx.coroutines.Job]?.cancelChildren()

            val finalState = machine.state.value
            assertEquals(0, finalState.cards.count { it.isFaceUp })
        }
}
