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
import kotlinx.coroutines.test.runCurrent
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

    @Test
    fun `ScanCards action flips unmatched face-down cards up and then back down after duration`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    cards =
                        persistentListOf(
                            CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = false, isMatched = false),
                            CardState(id = 1, suit = Suit.Spades, rank = Rank.King, isFaceUp = true, isMatched = true),
                            CardState(id = 2, suit = Suit.Clubs, rank = Rank.Two, isFaceUp = true, isMatched = false),
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

            val durationMs = 1000L
            stateMachine.dispatch(GameAction.ScanCards(durationMs))
            runCurrent()

            // Card 0 should be face up now (scanned)
            // Card 1 is already face up and matched
            // Card 2 is already face up
            var cards = stateMachine.state.value.cards
            assertTrue(cards[0].isFaceUp)
            assertTrue(cards[1].isFaceUp)
            assertTrue(cards[2].isFaceUp)

            // Advance time past the duration
            advanceTimeBy(durationMs + 1)
            runCurrent()

            // Card 0 should be back face down
            // Card 1 should remain face up (matched)
            // Card 2 should remain face down? Wait, looking at the code for ScanCards:
            // if (!card.isMatched && card.isFaceUp) { list[i] = card.copy(isFaceUp = false) }
            // This flips ALL unmatched face-up cards down! Including Card 2 which was face up before the scan!
            // We should assert this behavior.
            cards = stateMachine.state.value.cards
            assertFalse(cards[0].isFaceUp)
            assertTrue(cards[1].isFaceUp)
            assertFalse(cards[2].isFaceUp)
        }

    @Test
    fun `ScanCards action cancels existing scan when dispatched again`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    cards =
                        persistentListOf(
                            CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = false, isMatched = false),
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

            val durationMs1 = 1000L
            val durationMs2 = 2000L

            stateMachine.dispatch(GameAction.ScanCards(durationMs1))
            runCurrent()

            // State should be face up
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )

            // Advance time halfway through the first duration
            advanceTimeBy(500L)
            runCurrent()

            // Dispatch second scan
            stateMachine.dispatch(GameAction.ScanCards(durationMs2))
            runCurrent()

            // Still face up
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )

            // Advance past original duration (total time 1500L, original was 1000L)
            // At this point, the second job is 1000L into its 2000L delay
            advanceTimeBy(1000L)
            runCurrent()

            // Should STILL be face up because the first job was cancelled
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )

            // Advance past the new duration (total time since second scan: 500L + 1000L + 1000L = 2500L)
            advanceTimeBy(1000L + 1)
            runCurrent()

            // Now it should be face down
            assertFalse(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )
        }

    @Test
    fun `ScanCards does not affect already matched cards`() =
        runTest(testDispatcher) {
            val initialState =
                MemoryGameState(
                    cards =
                        persistentListOf(
                            CardState(id = 0, suit = Suit.Hearts, rank = Rank.Ace, isFaceUp = true, isMatched = true),
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

            stateMachine.dispatch(GameAction.ScanCards(1000L))
            runCurrent()

            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isMatched,
            )

            advanceTimeBy(1000L + 1)
            runCurrent()

            // Should remain face up
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isMatched,
            )
        }
}
