package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class GamePowerUpTest {
    @Test
    fun `fromIdOrName returns correct enum for valid id`() {
        assertEquals(GamePowerUp.TIME_BANK, GamePowerUp.fromIdOrName("powerup_timebank"))
        assertEquals(GamePowerUp.PEEK, GamePowerUp.fromIdOrName("powerup_peek"))
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("powerup_none"))
    }

    @Test
    fun `fromIdOrName returns correct enum for valid name`() {
        assertEquals(GamePowerUp.TIME_BANK, GamePowerUp.fromIdOrName("TIME_BANK"))
        assertEquals(GamePowerUp.PEEK, GamePowerUp.fromIdOrName("PEEK"))
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("NONE"))
    }

    @Test
    fun `fromIdOrName returns NONE for unknown value`() {
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("unknown_powerup"))
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("random_string"))
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName(""))
    }

    @Test
    fun `fromIdOrName is case sensitive and returns NONE for mismatched case`() {
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("powerup_Timebank"))
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("time_bank"))
        assertEquals(GamePowerUp.NONE, GamePowerUp.fromIdOrName("Peek"))
    }
}
