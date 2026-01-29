package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ScoringConfigTest {
    @Test
    fun `test default values`() {
        val config = ScoringConfig()
        assertEquals(100, config.baseMatchPoints)
        assertEquals(50, config.comboBonusPoints)
        assertEquals(3, config.heatModeThreshold)
        assertEquals(6, config.theNutsThreshold)
    }

    @Test
    fun `test custom values`() {
        val config =
            ScoringConfig(
                baseMatchPoints = 200,
                comboBonusPoints = 100,
                heatModeThreshold = 5,
                theNutsThreshold = 10,
            )
        assertEquals(200, config.baseMatchPoints)
        assertEquals(100, config.comboBonusPoints)
        assertEquals(5, config.heatModeThreshold)
        assertEquals(10, config.theNutsThreshold)
    }

    @Test
    fun `test copy creates new instance with modified values`() {
        val config1 = ScoringConfig()
        val config2 = config1.copy(baseMatchPoints = 500)

        assertEquals(100, config1.baseMatchPoints)
        assertEquals(500, config2.baseMatchPoints)
        assertNotEquals(config1, config2)
    }

    @Test
    fun `test equality`() {
        val config1 = ScoringConfig(baseMatchPoints = 100)
        val config2 = ScoringConfig(baseMatchPoints = 100)
        val config3 = ScoringConfig(baseMatchPoints = 200)

        assertEquals(config1, config2)
        assertNotEquals(config1, config3)
    }

    @Test
    fun `test hashCode`() {
        val config1 = ScoringConfig(baseMatchPoints = 100)
        val config2 = ScoringConfig(baseMatchPoints = 100)
        assertEquals(config1.hashCode(), config2.hashCode())
    }

    @Test
    fun `test toString`() {
        val config = ScoringConfig(baseMatchPoints = 123)
        assertTrue(config.toString().contains("123"))
    }

    @Test
    fun `test full copy and destructuring`() {
        val config = ScoringConfig()
        val (base, _, _, _, _, _, _, _, nuts) = config

        assertEquals(config.baseMatchPoints, base)
        assertEquals(config.theNutsThreshold, nuts)

        val config2 =
            config.copy(
                baseMatchPoints = 1,
                comboBonusPoints = 2,
                timeBonusPerPair = 3,
                timePenaltyPerSecond = 4,
                moveBonusMultiplier = 5,
                heatModeThreshold = 6,
                doubleDownPenalty = 7,
                highRollerThreshold = 8,
                theNutsThreshold = 9,
            )
        assertEquals(1, config2.baseMatchPoints)
        assertEquals(9, config2.theNutsThreshold)
    }
}
