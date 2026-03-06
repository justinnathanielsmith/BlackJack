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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest : BaseLogicTest() {
    @Test
    fun `initialization with negative time throws exception`() =
        runTest {
            assertFailsWith<IllegalArgumentException> {
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = testDispatchers,
                    initialState = MemoryGameState(),
                    initialTimeSeconds = -1L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )
            }
        }

    @Test
    fun `new game triggers initial save`() =
        runTest {
            var savedState: MemoryGameState? = null
            var savedTime: Long? = null

            val initialState = MemoryGameState()

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { state, time ->
                        savedState = state
                        savedTime = time
                    },
                    isResumed = false,
                )

            runCurrent()
            stateMachine.stopTimer()

            assertEquals(initialState, savedState)
            assertEquals(100L, savedTime)
        }

    @Test
    fun `resumed game does not trigger initial save`() =
        runTest {
            var saveCalled = false

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = testDispatchers,
                    initialState = MemoryGameState(),
                    initialTimeSeconds = 10L,
                    onSaveState = { _, _ -> saveCalled = true },
                    isResumed = true,
                )

            runCurrent()
            stateMachine.stopTimer()

            assertFalse(saveCalled, "Resumed game should not trigger an initial save")
        }

    @Test
    fun `initialization clears lastMatchedIds`() =
        runTest {
            val initialState =
                MemoryGameState(
                    lastMatchedIds = persistentListOf(1, 2),
                )

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
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

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
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

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
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

            val stateMachine =
                GameStateMachine(
                    scope = backgroundScope,
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
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

            assertEquals(0, countAfterReset)
        }

    @Test
    fun `initialization for new game with error schedules ProcessMismatch`() =
        runTest {
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
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            runCurrent()
            advanceTimeBy(GameStateMachine.MISMATCH_DELAY_MS + 10)
            runCurrent()

            val card =
                stateMachine.state.value.cards
                    .first()
            assertFalse(card.isError)
            stateMachine.stopTimer()
        }

    @Test
    fun `ScanCards action flips unmatched face-down cards up and then back down after duration`() =
        runTest {
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
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            val durationMs = 1000L
            stateMachine.dispatch(GameAction.ScanCards(durationMs))
            runCurrent()

            var cards = stateMachine.state.value.cards
            assertTrue(cards[0].isFaceUp)
            assertTrue(cards[1].isFaceUp)
            assertTrue(cards[2].isFaceUp)

            advanceTimeBy(durationMs + 10)
            runCurrent()

            cards = stateMachine.state.value.cards
            assertFalse(cards[0].isFaceUp)
            assertTrue(cards[1].isFaceUp)
            assertFalse(cards[2].isFaceUp)
            stateMachine.stopTimer()
        }

    @Test
    fun `ScanCards action cancels existing scan when dispatched again`() =
        runTest {
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
                    dispatchers = testDispatchers,
                    initialState = initialState,
                    initialTimeSeconds = 100L,
                    onSaveState = { _, _ -> },
                    isResumed = false,
                )

            val durationMs1 = 1000L
            val durationMs2 = 2000L

            stateMachine.dispatch(GameAction.ScanCards(durationMs1))
            runCurrent()

            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )

            advanceTimeBy(500L)
            runCurrent()

            stateMachine.dispatch(GameAction.ScanCards(durationMs2))
            runCurrent()

            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )

            advanceTimeBy(1000L)
            runCurrent()

            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )

            advanceTimeBy(1000L + 10)
            runCurrent()

            assertFalse(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )
            stateMachine.stopTimer()
        }

    @Test
    fun `ScanCards does not affect already matched cards`() =
        runTest {
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
                    dispatchers = testDispatchers,
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

            advanceTimeBy(1000L + 10)
            runCurrent()

            assertTrue(
                stateMachine.state.value.cards[0]
                    .isFaceUp,
            )
            assertTrue(
                stateMachine.state.value.cards[0]
                    .isMatched,
            )
            stateMachine.stopTimer()
        }
}
