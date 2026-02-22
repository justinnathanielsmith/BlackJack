package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeAttackLogicTest {
    @Test
    fun `calculateInitialTime returns configured time for mapped pair counts`() {
        val config =
            ScoringConfig(
                timeAttackInitialTimeMap =
                    mapOf(
                        6 to 100L,
                        8 to 200L,
                    ),
            )

        assertEquals(100L, TimeAttackLogic.calculateInitialTime(6, config))
        assertEquals(200L, TimeAttackLogic.calculateInitialTime(8, config))
    }

    @Test
    fun `calculateInitialTime uses fallback for unmapped pair counts`() {
        val config =
            ScoringConfig(
                timeAttackInitialTimeMap =
                    mapOf(
                        6 to 100L,
                        8 to 200L,
                    ),
            )

        // Fallback logic: pairCount * 4L
        assertEquals(4 * 4L, TimeAttackLogic.calculateInitialTime(4, config))
        assertEquals(10 * 4L, TimeAttackLogic.calculateInitialTime(10, config))
    }

    @Test
    fun `calculateTimeGain returns correct time based on combo multiplier`() {
        val config =
            ScoringConfig(
                timeAttackBaseGain = 5,
                timeAttackComboBonusMultiplier = 3,
            )

        // Case 1: Combo 1 (Base gain only)
        assertEquals(5, TimeAttackLogic.calculateTimeGain(1, config))

        // Case 2: Combo 2 (Base + 1 * Bonus)
        assertEquals(5 + 3, TimeAttackLogic.calculateTimeGain(2, config))

        // Case 3: Combo 5 (Base + 4 * Bonus)
        assertEquals(5 + 4 * 3, TimeAttackLogic.calculateTimeGain(5, config))

        // Case 4: Combo 0 (Should not apply negative bonus, effectively base gain)
        assertEquals(5, TimeAttackLogic.calculateTimeGain(0, config))
    }

    @Test
    fun `addBonusTime adds seconds correctly`() {
        assertEquals(15L, TimeAttackLogic.addBonusTime(10L, 5))
    }

    @Test
    fun `addBonusTime handles negative results by clamping to zero`() {
        assertEquals(0L, TimeAttackLogic.addBonusTime(10L, -15))
    }

    @Test
    fun `addBonusTime handles zero bonus`() {
        assertEquals(10L, TimeAttackLogic.addBonusTime(10L, 0))
    }
}
