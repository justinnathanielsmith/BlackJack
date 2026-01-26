package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoreBreakdownTest {

    @Test
    fun testDefaults() {
        val breakdown = ScoreBreakdown()
        assertEquals(0, breakdown.matchPoints)
        assertEquals(0, breakdown.timeBonus)
        assertEquals(0, breakdown.moveBonus)
        assertEquals(0, breakdown.totalScore)
    }

    @Test
    fun testCustomValues() {
        val breakdown = ScoreBreakdown(
            matchPoints = 100,
            timeBonus = 50,
            moveBonus = 20,
            totalScore = 170,
        )
        assertEquals(100, breakdown.matchPoints)
        assertEquals(50, breakdown.timeBonus)
        assertEquals(20, breakdown.moveBonus)
        assertEquals(170, breakdown.totalScore)
    }
}
