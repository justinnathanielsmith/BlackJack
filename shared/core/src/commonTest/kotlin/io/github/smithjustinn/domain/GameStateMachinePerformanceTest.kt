package io.github.smithjustinn.domain

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.usecases.economy.EarnCurrencyUseCase
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachinePerformanceTest : BaseLogicTest() {

    @Test
    fun `benchmark onSaveState calls during ticks`() = runTest {
        var saveCount = 0
        val onSaveState: (MemoryGameState, Long) -> Unit = { _, _ ->
            saveCount++
        }

        val machine = createStateMachine(onSaveState = onSaveState)
        machine.dispatch(GameAction.StartGame())

        // Initial save is called in init
        assertEquals(1, saveCount, "Should save on init")

        // Simulate 10 seconds of gameplay
        // Ticks occur every 1 second.
        repeat(10) {
            advanceTimeBy(1001)
        }

        // With 2000ms debounce, we expect roughly 1 save every 2 seconds.
        // 10 seconds / 2 = 5 saves. + 1 init = 6.
        // It should be significantly less than 11.
        println("Save count: $saveCount")
        assertTrue(saveCount < 8, "Should save significantly less than every tick (expected ~6, got $saveCount)")
        assertTrue(saveCount >= 4, "Should still save periodically (expected ~6, got $saveCount)")
    }

    private fun createStateMachine(
        initialState: MemoryGameState = MemoryGameState(mode = GameMode.TIME_ATTACK),
        initialTimeSeconds: Long = 60L,
        onSaveState: (MemoryGameState, Long) -> Unit = { _, _ -> },
    ) = GameStateMachine(
        scope = testScope,
        dispatchers = testDispatchers,
        initialState = initialState,
        initialTimeSeconds = initialTimeSeconds,
        earnCurrencyUseCase = mock<EarnCurrencyUseCase>().also { everySuspend { it.execute(any()) } returns Unit },
        onSaveState = onSaveState,
    )
}
