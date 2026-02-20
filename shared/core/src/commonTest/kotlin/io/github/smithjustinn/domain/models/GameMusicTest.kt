package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertEquals

class GameMusicTest {
    // Note: Currently GameMusic only has a DEFAULT entry.
    // These tests verify that lookups work correctly for the single existing entry
    // and correctly fallback to DEFAULT for invalid inputs.
    // If more music types are added in the future, please add test cases for them.

    @Test
    fun `fromIdOrName returns default for valid id`() {
        assertEquals(GameMusic.DEFAULT, GameMusic.fromIdOrName("music_default"))
    }

    @Test
    fun `fromIdOrName returns default for valid name`() {
        assertEquals(GameMusic.DEFAULT, GameMusic.fromIdOrName("DEFAULT"))
    }

    @Test
    fun `fromIdOrName returns default for invalid input`() {
        assertEquals(GameMusic.DEFAULT, GameMusic.fromIdOrName("invalid_value"))
    }

    @Test
    fun `fromIdOrName returns default for empty string`() {
        assertEquals(GameMusic.DEFAULT, GameMusic.fromIdOrName(""))
    }
}
