package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateSyncManagerTest : BaseLogicTest() {

    @Test
    fun `normal priority sync debounces requests`() = runTest {
        var saveCount = 0
        val savedStates = mutableListOf<MemoryGameState>()
        val manager = GameStateSyncManager(
            scope = testScope,
            dispatchers = testDispatchers,
            onSave = { state, _ ->
                saveCount++
                savedStates.add(state)
            }
        )

        val state1 = MemoryGameState(moves = 1)
        val state2 = MemoryGameState(moves = 2)

        manager.sync(state1, 100L)
        advanceTimeBy(1000) // Halfway through debounce
        assertEquals(0, saveCount, "Should not save yet")

        manager.sync(state2, 90L)
        advanceTimeBy(1000) // Previous debounce would have fired, but should be cancelled
        assertEquals(0, saveCount, "Should still not save due to debounce reset")

        advanceTimeBy(1001) // Finish second debounce
        assertEquals(1, saveCount, "Should save once")
        assertEquals(state2, savedStates.last(), "Should save the latest state")
    }

    @Test
    fun `high priority sync saves immediately`() = runTest {
        var saveCount = 0
        val manager = GameStateSyncManager(
            scope = testScope,
            dispatchers = testDispatchers,
            onSave = { _, _ -> saveCount++ }
        )

        val state = MemoryGameState(moves = 1)
        manager.sync(state, 100L, priority = GameStateSyncManager.Priority.HIGH)

        advanceUntilIdle()
        assertEquals(1, saveCount, "High priority should save immediately")
    }

    @Test
    fun `flush saves immediately and cancels pending debounce`() = runTest {
        var saveCount = 0
        val savedStates = mutableListOf<MemoryGameState>()
        val manager = GameStateSyncManager(
            scope = testScope,
            dispatchers = testDispatchers,
            onSave = { state, _ ->
                saveCount++
                savedStates.add(state)
            }
        )

        val state1 = MemoryGameState(moves = 1)
        val state2 = MemoryGameState(moves = 2)

        manager.sync(state1, 100L)
        advanceTimeBy(1000)

        manager.flush(state2, 90L)
        advanceUntilIdle()

        assertEquals(1, saveCount, "Flush should save immediately")
        assertEquals(state2, savedStates.last(), "Flush should save state2")

        advanceTimeBy(2000) // Wait for original debounce time
        assertEquals(1, saveCount, "Original debounce should be cancelled/ignored")
    }
}
