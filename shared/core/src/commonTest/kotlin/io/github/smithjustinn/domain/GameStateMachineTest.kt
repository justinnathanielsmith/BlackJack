package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest {
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatchers =
        CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )

    @Test
    fun `initialization throws IllegalArgumentException if initialTimeSeconds is negative`() =
        runTest(testDispatcher) {
            var exceptionThrown = false
            try {
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = dispatchers,
                    initialState = MemoryGameState(),
                    initialTimeSeconds = -1L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )
            } catch (e: IllegalArgumentException) {
                exceptionThrown = true
                assertEquals("Initial time cannot be negative", e.message)
            }
            assertTrue(exceptionThrown, "Expected IllegalArgumentException to be thrown")
        }

    @Test
    fun `initialization for new game triggers initial save`() =
        runTest(testDispatcher) {
            var savedState: MemoryGameState? = null
            var savedTime: Long? = null

            val initialState = MemoryGameState()

            GameStateMachine(
                scope = backgroundScope,
                dispatchers = dispatchers,
                initialState = initialState,
                initialTimeSeconds = 100L,
                onSaveState = { state, time ->
                    savedState = state
                    savedTime = time
                },
                isResumed = false,
            )

            advanceUntilIdle()

            assertEquals(initialState, savedState)
            assertEquals(100L, savedTime)
        }

    @Test
    fun `initialization for resumed game does NOT trigger initial save`() =
        runTest(testDispatcher) {
            var saveCalled = false

            GameStateMachine(
                scope = backgroundScope,
                dispatchers = dispatchers,
                initialState = MemoryGameState(),
                initialTimeSeconds = 100L,
                onSaveState = { _, _ -> saveCalled = true },
                isResumed = true,
            )

            advanceUntilIdle()

            assertFalse(saveCalled, "Save should not be called when resuming a game")
        }

    @Test
    fun `initialization clears lastMatchedIds`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    lastMatchedIds = persistentListOf(1, 2),
                )

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = dispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            advanceUntilIdle()

            assertTrue(
                stateMachine.state.value.lastMatchedIds
                    .isEmpty(),
            )
        }

    @Test
    fun `initialization for new game with error schedules ProcessMismatch`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    cards =
                        persistentListOf(
                            CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isError = true),
                            CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isError = false),
                        ),
                )

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = dispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 1)
            advanceUntilIdle()

            val card =
                stateMachine.state.value.cards
                    .first()
            assertFalse(card.isError)
        }

    @Test
    fun `initialization for new game with 2 face up unmatched cards schedules ProcessMismatch`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    cards =
                        persistentListOf(
                            CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = false),
                            CardState(
                                id = 1,
                                suit = Suit.Diamonds,
                                rank = Rank.Two,
                                isFaceUp = true,
                                isMatched = false,
                            ),
                        ),
                )

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = dispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 1)
            advanceUntilIdle()

            val cards = stateMachine.state.value.cards
            assertTrue(cards.all { !it.isFaceUp })
        }

    @Test
    fun `initialization for resumed game with errors does NOT schedule ProcessMismatch`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    cards =
                        persistentListOf(
                            CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isError = true),
                        ),
                )

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = dispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = true, // RESUMED
                )

            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 1)
            advanceUntilIdle()

            val card =
                stateMachine.state.value.cards
                    .first()
            assertTrue(card.isError)
        }
}
