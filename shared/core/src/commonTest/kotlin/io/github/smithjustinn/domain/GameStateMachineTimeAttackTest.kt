package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTimeAttackTest : BaseLogicTest() {
    @Test
    fun `handleTick in TIME_ATTACK mode decrements time and emits TimerUpdate`() =
        runTest {
            val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
            val result =
                gameStateMachine(initialState, 10L) {
                    val currentState = initialState
                    if (currentState.mode == GameMode.TIME_ATTACK) {
                        updateTime { (it - 1).coerceAtLeast(0) }
                        val nextTime = (10L - 1).coerceAtLeast(0)
                        +GameEffect.TimerUpdate(nextTime)
                    }
                }

            // Should emit TimerUpdate with 9
            assertTrue(result.effects.contains(GameEffect.TimerUpdate(9L)))
            assertFalse(result.state.isGameOver)
        }

    @Test
    fun `handleTick in TIME_ATTACK mode emits VibrateTick when time is low`() =
        runTest {
            val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
            val result =
                gameStateMachine(initialState, 6L) {
                    val currentState = initialState
                    if (currentState.mode == GameMode.TIME_ATTACK) {
                        updateTime { (it - 1).coerceAtLeast(0) }
                        val nextTime = (6L - 1).coerceAtLeast(0)
                        +GameEffect.TimerUpdate(nextTime)
                        if (nextTime <= 5L) { // LOW_TIME_WARNING_THRESHOLD
                            +GameEffect.VibrateTick
                        }
                    }
                }

            // Should emit TimerUpdate and VibrateTick
            assertTrue(result.effects.contains(GameEffect.TimerUpdate(5L)))
            assertTrue(result.effects.contains(GameEffect.VibrateTick))
            assertFalse(result.state.isGameOver)
        }

    @Test
    fun `handleTick in TIME_ATTACK mode ends game when time reaches zero`() =
        runTest {
            val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK, score = 100)
            val result =
                gameStateMachine(initialState, 1L) {
                    val currentState = initialState
                    if (currentState.mode == GameMode.TIME_ATTACK) {
                        updateTime { (it - 1).coerceAtLeast(0) }
                        val nextTime = (1L - 1).coerceAtLeast(0)
                        +GameEffect.TimerUpdate(nextTime)

                        if (nextTime <= 0) {
                            +GameEffect.VibrateWarning
                            +GameEffect.PlayLoseSound
                            transition { currentState.copy(isGameOver = true, isGameWon = false, score = 0) }
                            +GameEffect.GameOver
                        }
                    }
                }

            // Assert state
            val finalState = result.state
            assertTrue(finalState.isGameOver, "Expected game over")
            assertFalse(finalState.isGameWon, "Expected not game won")
            assertEquals(0, finalState.score)

            // Assert effects
            assertTrue(result.effects.contains(GameEffect.TimerUpdate(0L)))
            assertTrue(result.effects.contains(GameEffect.VibrateWarning))
            assertTrue(result.effects.contains(GameEffect.PlayLoseSound))
            assertTrue(result.effects.contains(GameEffect.GameOver))
        }

    @Test
    fun `handleTick ignores tick if game is already over`() =
        runTest {
            val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK, isGameOver = true)
            val result =
                gameStateMachine(initialState, 10L) {
                    val currentState = initialState
                    if (currentState.isGameOver) return@gameStateMachine
                }

            // The state time should remain 10L, and there should be no change
            assertEquals(true, result.state.isGameOver)
            assertEquals(0, result.effects.size)
        }
}
