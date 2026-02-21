package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateSyncManagerTest : BaseLogicTest() {
    @Test
    fun `normal priority sync samples requests`() =
        runTest {
            var saveCount = 0
            val savedStates = mutableListOf<MemoryGameState>()
            val managerScope = CoroutineScope(testDispatcher + Job())
            try {
                val manager =
                    GameStateSyncManager(
                        scope = managerScope,
                        dispatchers = testDispatchers,
                        onSave = { state, _ ->
                            saveCount++
                            savedStates.add(state)
                        },
                    )

                val state1 = MemoryGameState(moves = 1)
                val state2 = MemoryGameState(moves = 2)

                manager.sync(state1, 100L)
                advanceTimeBy(1000) // Halfway through sample period
                assertEquals(0, saveCount, "Should not save yet")

                manager.sync(state2, 90L)
            advanceTimeBy(1001) // Sample period ends (2000ms total)
                assertEquals(1, saveCount, "Should save at the end of sample period")
                assertEquals(state2, savedStates.last(), "Should save the latest state")
            } finally {
                managerScope.cancel()
            }
        }

    @Test
    fun `high priority sync saves immediately`() =
        runTest {
            var saveCount = 0
            val managerScope = CoroutineScope(testDispatcher + Job())
            try {
                val manager =
                    GameStateSyncManager(
                        scope = managerScope,
                        dispatchers = testDispatchers,
                        onSave = { _, _ -> saveCount++ },
                    )

                val state = MemoryGameState(moves = 1)
                manager.sync(state, 100L, priority = GameStateSyncManager.Priority.HIGH)

            advanceTimeBy(100)
                assertEquals(1, saveCount, "High priority should save immediately")
            } finally {
                managerScope.cancel()
            }
        }

    @Test
    fun `flush saves immediately and ignores pending sample if older`() =
        runTest {
            var saveCount = 0
            val savedStates = mutableListOf<MemoryGameState>()
            val managerScope = CoroutineScope(testDispatcher + Job())
            try {
                val manager =
                    GameStateSyncManager(
                        scope = managerScope,
                        dispatchers = testDispatchers,
                        onSave = { state, _ ->
                            saveCount++
                            savedStates.add(state)
                        },
                    )

                val state1 = MemoryGameState(moves = 1)
                val state2 = MemoryGameState(moves = 2)

                manager.sync(state1, 100L)
                advanceTimeBy(1000)

                manager.flush(state2, 90L)
            advanceTimeBy(100)

                assertEquals(1, saveCount, "Flush should save immediately")
                assertEquals(state2, savedStates.last(), "Flush should save state2")

                advanceTimeBy(2000) // Wait for original sample time
                assertEquals(1, saveCount, "Original sample should be ignored as it is older")
            } finally {
                managerScope.cancel()
            }
        }

    @Test
    fun `continuous normal priority sync should save periodically`() =
        runTest {
            var saveCount = 0
            val managerScope = CoroutineScope(testDispatcher + Job())
            try {
                val manager =
                    GameStateSyncManager(
                        scope = managerScope,
                        dispatchers = testDispatchers,
                        onSave = { _, _ -> saveCount++ },
                    )

                // Simulate continuous updates every 1 second for 5 seconds
                repeat(5) { i ->
                    val state = MemoryGameState(moves = i)
                    manager.sync(state, 100L * i)
                    advanceTimeBy(1000)
                }

                // With sample(2000), saves should happen roughly every 2 seconds.
                // Expected saves: at least 2.
                assertTrue(saveCount >= 2, "Should save at least twice during 5 seconds of continuous updates. Actual: \$saveCount")
            } finally {
                managerScope.cancel()
            }
        }
}
