package io.github.smithjustinn.utils

import kotlin.test.Test
import kotlin.test.assertNotNull

class UtilsTest {

    @Test
    fun `LoggingContainer provides logger`() {
        assertNotNull(LoggingContainer.getLogger())
    }
}
