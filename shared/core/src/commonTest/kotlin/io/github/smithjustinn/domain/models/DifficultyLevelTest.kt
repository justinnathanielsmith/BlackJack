package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class DifficultyLevelTest {

    @Test
    fun `defaultLevels contains all levels`() {
        assertEquals(4, DifficultyLevel.defaultLevels.size)
        assertEquals(6, DifficultyLevel.defaultLevels[0].pairs)
        assertEquals(8, DifficultyLevel.defaultLevels[1].pairs)
        assertEquals(10, DifficultyLevel.defaultLevels[2].pairs)
        assertEquals(12, DifficultyLevel.defaultLevels[3].pairs)
    }
}
