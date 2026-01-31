package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreBreakdownTest {
    @Test
    fun testDefaults() {
        val breakdown = ScoreBreakdown()
        assertEquals(0, breakdown.basePoints)
        assertEquals(0, breakdown.timeBonus)
        assertEquals(0, breakdown.moveBonus)
        assertEquals(0, breakdown.totalScore)
    }

    @Test
    fun testCustomValues() {
        val breakdown =
            ScoreBreakdown(
                basePoints = 100,
                timeBonus = 50,
                moveBonus = 20,
                totalScore = 170,
            )
        assertEquals(100, breakdown.basePoints)
        assertEquals(50, breakdown.timeBonus)
        assertEquals(20, breakdown.moveBonus)
        assertEquals(170, breakdown.totalScore)
    }
}
