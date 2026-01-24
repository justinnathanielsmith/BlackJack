package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.GameMode
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class DailyChallengeLogicTest {

    @Test
    fun `test seeded generation produces identical games`() {
        val seed = 12345L
        
        // Game 1
        val random1 = Random(seed)
        val game1 = MemoryGameLogic.createInitialState(
            pairCount = 8,
            mode = GameMode.DAILY_CHALLENGE,
            random = random1
        )
        
        // Game 2
        val random2 = Random(seed)
        val game2 = MemoryGameLogic.createInitialState(
            pairCount = 8,
            mode = GameMode.DAILY_CHALLENGE,
            random = random2
        )
        
        // Check cards are identical
        assertEquals(game1.cards.size, game2.cards.size)
        game1.cards.forEachIndexed { index, card1 ->
            val card2 = game2.cards[index]
            assertEquals(card1.suit, card2.suit)
            assertEquals(card1.rank, card2.rank)
        }
    }

    @Test
    fun `test different seeds produce different games`() {
        val seed1 = 12345L
        val seed2 = 67890L
        
        val random1 = Random(seed1)
        val game1 = MemoryGameLogic.createInitialState(
            pairCount = 8,
            mode = GameMode.DAILY_CHALLENGE,
            random = random1
        )
        
        val random2 = Random(seed2)
        val game2 = MemoryGameLogic.createInitialState(
            pairCount = 8,
            mode = GameMode.DAILY_CHALLENGE,
            random = random2
        )
        
        // It's statistically extremely unlikely to be identical
        assertNotEquals(game1.cards, game2.cards)
    }
}
