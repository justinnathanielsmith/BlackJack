package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)
    private val dispatchers = CoroutineDispatchers(
        main = testDispatcher,
        mainImmediate = testDispatcher,
        io = testDispatcher,
        default = testDispatcher
    )

    @Test
    fun `initial state is correct`() = testScope.runTest {
        val initialState = MemoryGameState(mode = GameMode.TIME_ATTACK)
        val machine = GameStateMachine(
            scope = testScope,
            dispatchers = dispatchers,
            initialState = initialState,
            initialTimeSeconds = 60,
            timeProvider = { 1000L },
            onSaveState = { _, _ -> }
        )

        assertEquals(initialState, machine.state.value)
    }
}
