package io.github.smithjustinn.data.local

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlin.time.Instant

class ConvertersTest {
    private val converters = Converters()

    @Test
    fun testFromTimestamp() {
        val timestamp = 1672531200000L // 2023-01-01T00:00:00Z
        val expected = Instant.fromEpochMilliseconds(timestamp)
        assertEquals(expected, converters.fromTimestamp(timestamp))
        assertNull(converters.fromTimestamp(null))
    }

    @Test
    fun testDateToTimestamp() {
        val timestamp = 1672531200000L
        val instant = Instant.fromEpochMilliseconds(timestamp)
        assertEquals(timestamp, converters.dateToTimestamp(instant))
        assertNull(converters.dateToTimestamp(null))
    }

    @Test
    fun testFromGameState() {
        val gameState =
            MemoryGameState(
                pairCount = 8,
                mode = GameMode.TIME_ATTACK,
            )

        val json = converters.fromGameState(gameState)
        assertNotNull(json)
        // Verify it's valid JSON containing expected fields
        assertTrue(json.contains("pairCount"))
        assertTrue(json.contains("TIME_ATTACK"))
    }

    @Test
    fun testFromGameState_null() {
        assertNull(converters.fromGameState(null))
    }

    @Test
    fun testToGameState() {
        val json =
            """
            {
                "pairCount": 8,
                "mode": "TIME_ATTACK",
                "cards": [],
                "flippedIndices": [],
                "matchedPairs": [],
                "moves": 0,
                "timeElapsedSeconds": 0,
                "isPeekActive": false,
                "isGameOver": false,
                "currentScore": 0
            }
            """.trimIndent()

        val gameState = converters.toGameState(json)
        assertNotNull(gameState)
        assertEquals(8, gameState.pairCount)
        assertEquals(GameMode.TIME_ATTACK, gameState.mode)
    }

    @Test
    fun testToGameState_null() {
        assertNull(converters.toGameState(null))
    }

    @Test
    fun testGameStateRoundTrip() {
        val original =
            MemoryGameState(
                pairCount = 12,
                mode = GameMode.DAILY_CHALLENGE,
            )

        val json = converters.fromGameState(original)
        assertNotNull(json)

        val restored = converters.toGameState(json)
        assertNotNull(restored)
        assertEquals(original.pairCount, restored.pairCount)
        assertEquals(original.mode, restored.mode)
    }
}
