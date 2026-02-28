package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.cancel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest : BaseLogicTest() {

    @Test
    fun `initialization with negative time throws exception`() = runTest {
        val machineScope = CoroutineScope(testDispatcher + Job())
        assertFailsWith<IllegalArgumentException> {
            GameStateMachine(
                scope = machineScope,
                dispatchers = testDispatchers,
                initialState = MemoryGameState(),
                initialTimeSeconds = -1L,
                onSaveState = { _, _ -> },
                isResumed = false
            )
        }
        machineScope.cancel()
    }

    @Test
    fun `new game triggers initial save`() = runTest {
        var savedState: MemoryGameState? = null
        var savedTime: Long? = null

        val initialState = MemoryGameState()

        val machineScope = CoroutineScope(testDispatcher + Job())
        val stateMachine = GameStateMachine(
            scope = machineScope,
            dispatchers = testDispatchers,
            initialState = initialState,
            initialTimeSeconds = 0L,
            onSaveState = { state, time ->
                savedState = state
                savedTime = time
            },
            isResumed = false
        )

        runCurrent()
        stateMachine.stopTimer()
        machineScope.cancel()

        assertEquals(initialState, savedState)
        assertEquals(0L, savedTime)
    }

    @Test
    fun `resumed game does not trigger initial save`() = runTest {
        var saveCalled = false

        val machineScope = CoroutineScope(testDispatcher + Job())
        val stateMachine = GameStateMachine(
            scope = machineScope,
            dispatchers = testDispatchers,
            initialState = MemoryGameState(),
            initialTimeSeconds = 10L,
            onSaveState = { _, _ -> saveCalled = true },
            isResumed = true
        )

        runCurrent()
        stateMachine.stopTimer()
        machineScope.cancel()

        assertFalse(saveCalled, "Resumed game should not trigger an initial save")
    }

    @Test
    fun `initialization clears lastMatchedIds`() = runTest {
        val initialState = MemoryGameState(
            lastMatchedIds = persistentListOf(1, 2)
        )

        val machineScope = CoroutineScope(testDispatcher + Job())
        val stateMachine = GameStateMachine(
            scope = machineScope,
            dispatchers = testDispatchers,
            initialState = initialState,
            initialTimeSeconds = 0L,
            onSaveState = { _, _ -> },
            isResumed = false
        )

        runCurrent()
        val isEmpty = stateMachine.state.value.lastMatchedIds.isEmpty()

        stateMachine.stopTimer()
        machineScope.cancel()

        assertTrue(isEmpty, "lastMatchedIds should be cleared on initialization")
    }

    @Test
    fun `new game with mismatches schedules reset`() = runTest {
        val cards = persistentListOf(
            CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
            CardState(id = 2, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = false)
        )
        val initialState = MemoryGameState(cards = cards)

        val machineScope = CoroutineScope(testDispatcher + Job())
        val stateMachine = GameStateMachine(
            scope = machineScope,
            dispatchers = testDispatchers,
            initialState = initialState,
            initialTimeSeconds = 100L, // Enough time to prevent Game Over in Time Attack
            onSaveState = { _, _ -> },
            isResumed = false
        )

        runCurrent()

        advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 10)
        runCurrent()

        val countAfterReset = stateMachine.state.value.cards.count { it.isFaceUp }

        stateMachine.stopTimer()
        machineScope.cancel()

        assertEquals(0, countAfterReset)
    }

    @Test
    fun `resumed game with mismatches does NOT schedule reset`() = runTest {
        val cards = persistentListOf(
            CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
            CardState(id = 2, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = false)
        )
        val initialState = MemoryGameState(cards = cards)

        val machineScope = CoroutineScope(testDispatcher + Job())
        val stateMachine = GameStateMachine(
            scope = machineScope,
            dispatchers = testDispatchers,
            initialState = initialState,
            initialTimeSeconds = 100L,
            onSaveState = { _, _ -> },
            isResumed = true
        )

        runCurrent()

        advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 10)
        runCurrent()

        val countAfterNoReset = stateMachine.state.value.cards.count { it.isFaceUp }

        stateMachine.stopTimer()
        machineScope.cancel()

        // Since isResumed = true, it does NOT schedule reset.
        assertEquals(2, countAfterNoReset)
    }

    @Test
    fun `new game with blackout mutator uses half mismatch delay`() = runTest {
        val cards = persistentListOf(
            CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
            CardState(id = 2, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = false)
        )
        val initialState = MemoryGameState(
            cards = cards,
            activeMutators = setOf(DailyChallengeMutator.BLACKOUT)
        )

        val machineScope = CoroutineScope(testDispatcher + Job())
        val stateMachine = GameStateMachine(
            scope = machineScope,
            dispatchers = testDispatchers,
            initialState = initialState,
            initialTimeSeconds = 100L, // Enough time to prevent Game Over in Time Attack
            onSaveState = { _, _ -> },
            isResumed = false
        )

        runCurrent()

        // Advance by half the delay
        advanceTimeBy((GameStateMachine.MISMATCH_DELAY_MS / 2) + 10)
        runCurrent()

        val countAfterReset = stateMachine.state.value.cards.count { it.isFaceUp }

        stateMachine.stopTimer()
        machineScope.cancel()

        assertEquals(0, countAfterReset)
    }
}
