package io.github.smithjustinn.domain
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
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

    @Test
    fun `initialization with negative time throws exception`() =
        runTest {
            val machineScope = CoroutineScope(testDispatcher + Job())
            assertFailsWith<IllegalArgumentException> {
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = MemoryGameState(),
                    initialTimeSeconds = -1L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )
            }
            machineScope.cancel()
        }

    @Test
    fun `new game triggers initial save`() =
        runTest {
            var savedState: MemoryGameState? = null
            var savedTime: Long? = null

            val initialState = MemoryGameState()

            val machineScope = CoroutineScope(testDispatcher + Job())
            val stateMachine =
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 0L,
                    onSaveState = { state, time ->
                        savedState = state
                        savedTime = time
                    },
                    isResumed = false,
                )

            runCurrent()
            stateMachine.stopTimer()
            machineScope.cancel()

            assertEquals(initialState, savedState)
            assertEquals(0L, savedTime)
        }

    @Test
    fun `resumed game does not trigger initial save`() =
        runTest {
            var saveCalled = false

            val machineScope = CoroutineScope(testDispatcher + Job())
            val stateMachine =
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = MemoryGameState(),
                    initialTimeSeconds = 10L,
                    onSaveState = { _, _ -> saveCalled = true },
                    isResumed = true,
                )

            runCurrent()
            stateMachine.stopTimer()
            machineScope.cancel()

            assertFalse(saveCalled, "Resumed game should not trigger an initial save")
        }

    @Test
    fun `initialization clears lastMatchedIds`() =
        runTest {
            val initialState =
                MemoryGameState(
                    lastMatchedIds = persistentListOf(1, 2),
                )

            val machineScope = CoroutineScope(testDispatcher + Job())
            val stateMachine =
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 0L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            runCurrent()
            val isEmpty =
                stateMachine.state.value.lastMatchedIds
                    .isEmpty()

            stateMachine.stopTimer()
            machineScope.cancel()

            assertTrue(isEmpty, "lastMatchedIds should be cleared on initialization")
        }

    @Test
    fun `new game with mismatches schedules reset`() =
        runTest {
            val cards =
                persistentListOf(
                    CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
                    CardState(id = 2, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = false),
                )
            val initialState = MemoryGameState(cards = cards)

            val machineScope = CoroutineScope(testDispatcher + Job())
            val stateMachine =
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L, // Enough time to prevent Game Over in Time Attack
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            runCurrent()

            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 10)
            runCurrent()

            val countAfterReset =
                stateMachine.state.value.cards
                    .count { it.isFaceUp }

            stateMachine.stopTimer()
            machineScope.cancel()

            assertEquals(0, countAfterReset)
        }

    @Test
    fun `resumed game with mismatches does NOT schedule reset`() =
        runTest {
            val cards =
                persistentListOf(
                    CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
                    CardState(id = 2, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = false),
                )
            val initialState = MemoryGameState(cards = cards)

            val machineScope = CoroutineScope(testDispatcher + Job())
            val stateMachine =
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = true,
                )

            runCurrent()

            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 10)
            runCurrent()

            val countAfterNoReset =
                stateMachine.state.value.cards
                    .count { it.isFaceUp }

            stateMachine.stopTimer()
            machineScope.cancel()

            // Since isResumed = true, it does NOT schedule reset.
            assertEquals(2, countAfterNoReset)
        }

    @Test
    fun `new game with blackout mutator uses half mismatch delay`() =
        runTest {
            val cards =
                persistentListOf(
                    CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
                    CardState(id = 2, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = false),
                )
            val initialState =
                MemoryGameState(
                    cards = cards,
                    activeMutators = setOf(DailyChallengeMutator.BLACKOUT),
                )

            val machineScope = CoroutineScope(testDispatcher + Job())
            val stateMachine =
                GameStateMachine(
                    scope = machineScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L, // Enough time to prevent Game Over in Time Attack
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            runCurrent()

            // Advance by half the delay
            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS / 2 + 10)
            runCurrent()

            val countAfterReset =
                stateMachine.state.value.cards
                    .count { it.isFaceUp }

            stateMachine.stopTimer()
            machineScope.cancel()

            assertEquals(0, countAfterReset)
        }
}
