package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreBreakdownTest {
    @Test
    fun `test default values`() {
        val breakdown = ScoreBreakdown()
        assertEquals(0, breakdown.totalScore)
        assertEquals(0, breakdown.basePoints)
    }

    @Test
    fun `test custom values`() {
        val breakdown =
            ScoreBreakdown(
                basePoints = 1000,
                comboBonus = 500,
                totalScore = 1500,
            )
        assertEquals(1000, breakdown.basePoints)
        assertEquals(500, breakdown.comboBonus)
        assertEquals(1500, breakdown.totalScore)
    }

    @Test
    fun `test copy`() {
        val breakdown1 = ScoreBreakdown(totalScore = 100)
        val breakdown2 = breakdown1.copy(totalScore = 200)

        assertEquals(100, breakdown1.totalScore)
        assertEquals(200, breakdown2.totalScore)
    }
}
