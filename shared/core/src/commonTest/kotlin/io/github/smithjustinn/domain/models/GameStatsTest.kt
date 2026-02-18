package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class GameStatsTest {
    @Test
    fun `init with valid inputs succeeds`() {
        val stats =
            GameStats(
                pairCount = 8,
                bestScore = 100,
                bestTimeSeconds = 60,
                gamesPlayed = 5,
            )
        assertEquals(8, stats.pairCount)
        assertEquals(100, stats.bestScore)
        assertEquals(60, stats.bestTimeSeconds)
        assertEquals(5, stats.gamesPlayed)
    }

    @Test
    fun `init with zero pair count fails`() {
        assertFailsWith<IllegalArgumentException> {
            GameStats(
                pairCount = 0,
                bestScore = 100,
                bestTimeSeconds = 60,
            )
        }
    }

    @Test
    fun `init with negative pair count fails`() {
        assertFailsWith<IllegalArgumentException> {
            GameStats(
                pairCount = -1,
                bestScore = 100,
                bestTimeSeconds = 60,
            )
        }
    }

    @Test
    fun `init with negative best score fails`() {
        assertFailsWith<IllegalArgumentException> {
            GameStats(
                pairCount = 8,
                bestScore = -1,
                bestTimeSeconds = 60,
            )
        }
    }

    @Test
    fun `init with negative best time fails`() {
        assertFailsWith<IllegalArgumentException> {
            GameStats(
                pairCount = 8,
                bestScore = 100,
                bestTimeSeconds = -1,
            )
        }
    }

    @Test
    fun `init with negative games played fails`() {
        assertFailsWith<IllegalArgumentException> {
            GameStats(
                pairCount = 8,
                bestScore = 100,
                bestTimeSeconds = 60,
                gamesPlayed = -1,
            )
        }
    }
}
