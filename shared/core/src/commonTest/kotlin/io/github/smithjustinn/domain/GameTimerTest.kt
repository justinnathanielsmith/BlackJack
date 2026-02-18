package io.github.smithjustinn.domain

import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GameTimerTest : BaseLogicTest() {
    @Test
    fun `timer ticks repeatedly`() =
        runTest {
            var tickCount = 0
            val timer =
                GameTimer(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    onTick = { tickCount++ },
                )

            timer.start()

            // Advance time by 1 tick interval + small buffer to ensure execution
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS + 1)
            assertEquals(1, tickCount)

            // Advance time by another tick interval
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS)
            assertEquals(2, tickCount)

            timer.stop()
        }

    @Test
    fun `timer stops correctly`() =
        runTest {
            var tickCount = 0
            val timer =
                GameTimer(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    onTick = { tickCount++ },
                )

            timer.start()

            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS + 1)
            assertEquals(1, tickCount)

            timer.stop()

            // Advance time further, tick count should not increase
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS * 5)
            assertEquals(1, tickCount)
        }

    @Test
    fun `restarting timer resets and doesn't duplicate ticks`() =
        runTest {
            var tickCount = 0
            val timer =
                GameTimer(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    onTick = { tickCount++ },
                )

            timer.start()
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS + 1)
            assertEquals(1, tickCount)

            // Restart timer
            timer.start()

            // Wait for less than interval, no new tick immediately
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS / 2)
            assertEquals(1, tickCount)

            // Wait for full interval from restart
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS / 2 + 2)
            assertEquals(2, tickCount)

            timer.stop()
        }

    @Test
    fun `stop before first tick prevents execution`() =
        runTest {
            var tickCount = 0
            val timer =
                GameTimer(
                    scope = testScope,
                    dispatchers = testDispatchers,
                    onTick = { tickCount++ },
                )

            timer.start()

            // Stop before first tick
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS / 2)
            timer.stop()

            // Advance past where first tick would have been
            advanceTimeBy(GameTimer.TIMER_TICK_INTERVAL_MS)
            assertEquals(0, tickCount)
        }
}
