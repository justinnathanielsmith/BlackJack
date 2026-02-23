package io.github.smithjustinn.ui.game

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import kotlin.test.Test
import kotlin.test.assertFailsWith

class GameArgsTest {
    @Test
    fun `should accept valid pair counts`() {
        val validCounts = listOf(2, 8, 26)
        validCounts.forEach { count ->
            GameArgs(
                pairCount = count,
                mode = GameMode.TIME_ATTACK,
                difficulty = DifficultyType.CASUAL,
                forceNewGame = true,
            )
        }
    }

    @Test
    fun `should throw exception for invalid pair counts`() {
        val invalidCounts = listOf(-10, 0, 1, 27, 52, 53, 100)
        invalidCounts.forEach { count ->
            assertFailsWith<IllegalArgumentException> {
                GameArgs(
                    pairCount = count,
                    mode = GameMode.TIME_ATTACK,
                    difficulty = DifficultyType.CASUAL,
                    forceNewGame = true,
                )
            }
        }
    }
}
