package io.github.smithjustinn.domain

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class HeatShieldBugTest : BaseLogicTest() {
    companion object {
        private const val INITIAL_TIME = 60L
        private const val MISMATCH_DELAY_MS = 1000L
    }

    @Test
    fun `heat shield used should reset cards after delay`() =
        runTest {
            val state = GameFactory.createInitialState(pairCount = 6, mode = GameMode.TIME_ATTACK)
                .copy(
                    isHeatShieldAvailable = true,
                    comboMultiplier = 1 // Heat shield only triggers if combo > 0
                )
            
            val firstCard = state.cards[0]
            val nonMatchCard = state.cards.drop(1).first { it.suit != firstCard.suit || it.rank != firstCard.rank }

            val machine = createStateMachine(initialState = state)

            machine.effects.test {
                // Flip first card
                machine.dispatch(GameAction.FlipCard(firstCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())

                // Flip second card (mismatch)
                machine.dispatch(GameAction.FlipCard(nonMatchCard.id))
                assertEquals(GameEffect.PlayFlipSound, awaitItem())
                assertEquals(GameEffect.HeatShieldUsed, awaitItem())

                // Verify Heat Shield was used
                val stateAfterShield = machine.state.value
                assertFalse(stateAfterShield.isHeatShieldAvailable, "Heat shield should be consumed")
                assertEquals(1, stateAfterShield.comboMultiplier, "Combo should NOT be reset")
                
                // CRITICAL: Check if cards are still face up and marked as error
                val flippedCards = stateAfterShield.cards.filter { it.isFaceUp && !it.isMatched }
                assertEquals(2, flippedCards.size, "Two cards should be face up")
                assertTrue(flippedCards.all { it.isError }, "Both cards should be in error state")

                // Wait for the delay
                advanceTimeBy(MISMATCH_DELAY_MS + 100)
                runCurrent() // Important to run the dispatched ProcessMismatch

                // In Time Attack, ProcessMismatch emits TimerUpdate and TimeLoss
                // Wrap in timeout to avoid hanging if they don't arrive
                kotlinx.coroutines.withTimeout(1000) {
                    val e1 = awaitItem()
                    val e2 = awaitItem()
                    assertTrue(e1 is GameEffect.TimerUpdate || e2 is GameEffect.TimerUpdate, "Expected TimerUpdate")
                    assertTrue(e1 is GameEffect.TimeLoss || e2 is GameEffect.TimeLoss, "Expected TimeLoss")
                }

                // If the bug exists, cards are STILL face up and in error state
                val finalState = machine.state.value
                val finalFlippedCards = finalState.cards.filter { it.isFaceUp && !it.isMatched }
                
                assertEquals(0, finalFlippedCards.size, "Cards should have been reset after delay")
                
                cancelAndIgnoreRemainingEvents()
            }
            
            advanceUntilIdle()
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
