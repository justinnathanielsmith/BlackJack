package io.github.smithjustinn.utils

import kotlin.test.Test
import kotlin.test.assertEquals

class TimeUtilsTest {
    @Test
    fun `formatTime should format zero seconds correctly`() {
        assertEquals("00:00", formatTime(0))
    }

    @Test
    fun `formatTime should format seconds under a minute correctly`() {
        assertEquals("00:45", formatTime(45))
    }

    @Test
    fun `formatTime should format exactly one minute correctly`() {
        assertEquals("01:00", formatTime(60))
    }

    @Test
    fun `formatTime should format multiple minutes correctly`() {
        assertEquals("12:34", formatTime(754))
    }

    @Test
    fun `formatTime should format more than 60 minutes correctly`() {
        // 3661 seconds = 1 hour, 1 minute, 1 second = 61 minutes, 1 second
        assertEquals("61:01", formatTime(3661))
    }
}
