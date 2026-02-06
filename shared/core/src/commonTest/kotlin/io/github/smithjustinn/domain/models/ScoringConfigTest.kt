package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class ScoringConfigTest {
    @Test
    fun testDefaultValues() {
        val config = ScoringConfig()
        assertEquals(100, config.baseMatchPoints)
        assertEquals(50, config.comboBonusPoints)
        assertEquals(50, config.timeBonusPerPair)
        assertEquals(1, config.timePenaltyPerSecond)
        assertEquals(10000, config.moveBonusMultiplier)
        assertEquals(3, config.heatModeThreshold)
        assertEquals(6, config.theNutsThreshold)
    }

    @Test
    fun testCustomValues() {
        val config =
            ScoringConfig(
                baseMatchPoints = 10,
                comboBonusPoints = 20,
                timeBonusPerPair = 30,
                timePenaltyPerSecond = 2,
                moveBonusMultiplier = 5000,
                heatModeThreshold = 3,
            )
        assertEquals(10, config.baseMatchPoints)
        assertEquals(20, config.comboBonusPoints)
        assertEquals(30, config.timeBonusPerPair)
        assertEquals(2, config.timePenaltyPerSecond)
        assertEquals(5000, config.moveBonusMultiplier)
        assertEquals(3, config.heatModeThreshold)
    }

    @Test
    fun testValidation() {
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(baseMatchPoints = 0)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(heatModeThreshold = 0)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(highRollerThreshold = 10, theNutsThreshold = 5)
        }
    }

    @Test
    fun testExtendedValidation() {
        // Pot mismatch penalty out of bounds
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(potMismatchPenalty = -0.1)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(potMismatchPenalty = 1.1)
        }

        // Negative penalties
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(timePenaltyPerSecond = -1)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(timeAttackMismatchPenalty = -1L)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(doubleDownPenalty = -100)
        }

        // Negative bonuses
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(comboBonusPoints = -50)
        }
        kotlin.test.assertFailsWith<IllegalArgumentException> {
            ScoringConfig(moveBonusMultiplier = -100)
        }
    }
}
