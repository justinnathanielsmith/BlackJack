package io.github.smithjustinn.data.local

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
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
}
