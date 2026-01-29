package io.github.smithjustinn.domain.models

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MemoryGameStateTest {
    @Test
    fun `test all properties and copy`() {
        val state =
            MemoryGameState(
                cards = persistentListOf(),
                pairCount = 6,
                score = 100,
                moves = 5,
            )

        val copied =
            state.copy(
                score = 200,
                isGameWon = true,
            )

        assertEquals(100, state.score)
        assertEquals(200, copied.score)
        assertTrue(copied.isGameWon)

        // Destructuring order must match constructor
        val (
            cards, pairs, _, _, _, score,
            _, _, _, _, _, _,
            _, _, _, _, _, _,
        ) = state
        assertEquals(state.cards, cards)
        assertEquals(state.score, score)
        assertEquals(state.pairCount, pairs)
    }

    @Test
    fun `test equality and hashCode`() {
        val state1 = MemoryGameState(cards = persistentListOf(), pairCount = 6)
        val state2 = MemoryGameState(cards = persistentListOf(), pairCount = 6)

        assertEquals(state1, state2)
        assertEquals(state1.hashCode(), state2.hashCode())
    }

    @Test
    fun `test toString`() {
        val state = MemoryGameState(cards = persistentListOf(), pairCount = 6, score = 999)
        assertTrue(state.toString().contains("999"))
    }

    @Test
    fun `test companion default`() {
        val default = MemoryGameState(persistentListOf(), 6)
        // Just verify it doesn't crash
        assertEquals(default.pairCount, 6)
    }
}
