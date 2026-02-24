package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.test.BaseLogicTest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameStateMachineResumeTest : BaseLogicTest() {
    private val initialTime = 60L

    @Test
    fun `resuming clears lastMatchedIds`() =
        runTest {
            val baseState = GameFactory.createInitialState(pairCount = 6)
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
