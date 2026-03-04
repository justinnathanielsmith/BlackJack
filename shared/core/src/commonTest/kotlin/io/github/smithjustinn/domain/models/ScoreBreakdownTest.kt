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

    @Test
    fun testNegativeValuesThrowException() {
        val exception1 =
            runCatching {
                ScoreBreakdown(basePoints = -1)
            }.exceptionOrNull()
        assertEquals("Base points cannot be negative", exception1?.message)

        val exception2 =
            runCatching {
                ScoreBreakdown(comboBonus = -1)
            }.exceptionOrNull()
        assertEquals("Combo bonus cannot be negative", exception2?.message)

        val exception3 =
            runCatching {
                ScoreBreakdown(doubleDownBonus = -1)
            }.exceptionOrNull()
        assertEquals("Double down bonus cannot be negative", exception3?.message)

        val exception4 =
            runCatching {
                ScoreBreakdown(timeBonus = -1)
            }.exceptionOrNull()
        assertEquals("Time bonus cannot be negative", exception4?.message)

        val exception5 =
            runCatching {
                ScoreBreakdown(moveBonus = -1)
            }.exceptionOrNull()
        assertEquals("Move bonus cannot be negative", exception5?.message)

        val exception6 =
            runCatching {
                ScoreBreakdown(dailyChallengeBonus = -1)
            }.exceptionOrNull()
        assertEquals("Daily challenge bonus cannot be negative", exception6?.message)

        val exception7 =
            runCatching {
                ScoreBreakdown(totalScore = -1)
            }.exceptionOrNull()
        assertEquals("Total score cannot be negative", exception7?.message)

        val exception8 =
            runCatching {
                ScoreBreakdown(earnedCurrency = -1)
            }.exceptionOrNull()
        assertEquals("Earned currency cannot be negative", exception8?.message)
    }
}
