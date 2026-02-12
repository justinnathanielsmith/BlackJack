package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.time.Instant

class LeaderboardEntryTest {
    @Test
    fun testValidation() {
        val t = Instant.fromEpochMilliseconds(1716384000000)

        // Invalid pairCount
        assertFailsWith<IllegalArgumentException> { LeaderboardEntry(pairCount = -1, score = 100, timeSeconds = 60, moves = 20, timestamp = t) }
        assertFailsWith<IllegalArgumentException> { LeaderboardEntry(pairCount = 0, score = 100, timeSeconds = 60, moves = 20, timestamp = t) }

        // Invalid score
        assertFailsWith<IllegalArgumentException> { LeaderboardEntry(pairCount = 8, score = -1, timeSeconds = 60, moves = 20, timestamp = t) }

        // Invalid time
        assertFailsWith<IllegalArgumentException> { LeaderboardEntry(pairCount = 8, score = 100, timeSeconds = -1, moves = 20, timestamp = t) }

        // Invalid moves
        assertFailsWith<IllegalArgumentException> { LeaderboardEntry(pairCount = 8, score = 100, timeSeconds = 60, moves = -1, timestamp = t) }
    }
}
