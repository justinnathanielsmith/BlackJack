package io.github.smithjustinn.domain

import dev.mokkery.matcher.any
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineResumeTest : BaseLogicTest() {
    private val initialTime = 60L
    private val mismatchDelayMs = 1000L

    @Test
    fun `resuming with error cards schedules ProcessMismatch`() =
        runTest {
            val baseState = MemoryGameLogic.createInitialState(pairCount = 6)
            val firstCard = baseState.cards[0]
            val secondCard = baseState.cards[1]

            // Simulate state saved during mismatch delay
            val resumedState =
                baseState.copy(
                    cards =
                        baseState.cards
                            .map { card ->
                                if (card.id == firstCard.id || card.id == secondCard.id) {
                                    card.copy(isFaceUp = true, isError = true)
                                } else {
                                    card
                                }
                            }.toPersistentList(),
                )

            val machine = createStateMachine(initialState = resumedState)

            // Initially they are still in error state
            assertTrue(
                machine.state.value.cards
                    .any { it.isError },
                "Cards should initially be in error state",
            )

            // Wait for mismatch delay
            advanceTimeBy(mismatchDelayMs + 1)

            // Now they should be reset
            assertTrue(
                machine.state.value.cards
                    .none { it.isError },
                "Cards should have been reset after delay",
            )
            assertTrue(
                machine.state.value.cards
                    .none { it.isFaceUp && !it.isMatched },
                "Cards should be face down after reset",
            )
        }

    @Test
    fun `resuming with multiple face-up unmatched cards schedules reset`() =
        runTest {
            val baseState = MemoryGameLogic.createInitialState(pairCount = 6)

            // Simulate state saved during ScanCards (multiple cards face up)
            val resumedState =
                baseState.copy(
                    cards =
                        baseState.cards
                            .mapIndexed { index, card ->
                                if (index < 4) {
                                    card.copy(isFaceUp = true)
                                } else {
                                    card
                                }
                            }.toPersistentList(),
                )

            val machine = createStateMachine(initialState = resumedState)

            // Initially they are still face up
            assertTrue(
                machine.state.value.cards
                    .count { it.isFaceUp && !it.isMatched } == 4,
            )

            // Wait for delay
            advanceTimeBy(mismatchDelayMs + 1)

            // Now they should be reset
            assertTrue(
                machine.state.value.cards
                    .none { it.isFaceUp && !it.isMatched },
                "Cards should be face down after reset",
            )
        }

    @Test
    fun `resuming clears lastMatchedIds`() =
        runTest {
            val baseState = MemoryGameLogic.createInitialState(pairCount = 6)
            val resumedState =
                baseState.copy(
                    lastMatchedIds = persistentListOf(1, 2),
                )

            val machine = createStateMachine(initialState = resumedState)

            // Should be cleared immediately in init
            assertTrue(
                machine.state.value.lastMatchedIds
                    .isEmpty(),
                "lastMatchedIds should be cleared on resume",
            )
        }

    private fun createStateMachine(
        initialState: MemoryGameState = MemoryGameState(mode = GameMode.TIME_ATTACK),
        initialTimeSeconds: Long = initialTime,
        onSaveState: (MemoryGameState, Long) -> Unit = { _, _ -> },
    ) = GameStateMachine(
        scope = testScope,
        dispatchers = testDispatchers,
        initialState = initialState,
        initialTimeSeconds = initialTimeSeconds,
        onSaveState = onSaveState,
        isResumed = true,
    )
}
