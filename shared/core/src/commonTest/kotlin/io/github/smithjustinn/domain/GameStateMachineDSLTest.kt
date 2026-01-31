package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState
import kotlin.test.Test
import kotlin.test.assertEquals

class GameStateMachineDSLTest {
    @Test
    fun `transitions are chained correctly`() {
        val initial = MemoryGameState(score = 0, comboMultiplier = 1)
        val result =
            gameStateMachine(initial, 100L) {
                transition { it.copy(score = 10) }
                transition { it.copy(comboMultiplier = 2) }
                updateTime { it + 5 }
                updateTime { it - 2 }
            }

        assertEquals(10, result.state.score)
        assertEquals(2, result.state.comboMultiplier)
        assertEquals(103L, result.time)
    }
}
