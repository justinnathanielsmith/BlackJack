package io.github.smithjustinn.domain.models

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class MemoryGameStateTest {
    @Test
    fun testSerialization() {
        val card =
            CardState(
                id = 1,
                suit = Suit.Hearts,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = false,
            )
        val state =
            MemoryGameState(
                cards = persistentListOf(card),
                score = 100,
                lastMatchedIds = persistentListOf(1, 2),
            )

        val json = Json.encodeToString(MemoryGameState.serializer(), state)
        val decoded = Json.decodeFromString(MemoryGameState.serializer(), json)

        assertEquals(state.cards.size, decoded.cards.size)
        assertEquals(state.cards[0].id, decoded.cards[0].id)
        assertEquals(state.score, decoded.score)
        assertEquals(state.lastMatchedIds.size, decoded.lastMatchedIds.size)
        assertEquals(state.lastMatchedIds[0], decoded.lastMatchedIds[0])
    }

    @Test
    fun testDefaults() {
        val state = MemoryGameState()
        assertEquals(0, state.score)
        assertEquals(0, state.moves)
        assertEquals(8, state.pairCount)
    }

    @Test
    fun testCopy() {
        val state = MemoryGameState()
        val copied = state.copy(score = 50)
        assertEquals(50, copied.score)
        assertEquals(state.cards, copied.cards)
        assertEquals(state.pairCount, copied.pairCount)
    }
}
