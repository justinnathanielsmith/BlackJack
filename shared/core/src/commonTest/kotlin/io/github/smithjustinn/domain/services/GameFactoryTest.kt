package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GameFactoryTest {

    @Test
    fun createInitialState_createsCorrectNumberOfCards() {
        val pairCount = 8
        val state = GameFactory.createInitialState(pairCount = pairCount)
        assertEquals(pairCount * 2, state.cards.size)
    }

    @Test
    fun createInitialState_createsMatchingPairs() {
        val pairCount = 5
        val state = GameFactory.createInitialState(pairCount = pairCount)

        // Group cards by (Suit, Rank)
        val groupedCards = state.cards.groupBy { it.suit to it.rank }

        // Assert each group has exactly 2 cards
        groupedCards.forEach { (key, cards) ->
            assertEquals(2, cards.size, "Found group with size ${cards.size} for $key")
        }

        // Assert we have exactly pairCount groups
        assertEquals(pairCount, groupedCards.size)
    }

    @Test
    fun createInitialState_assignsUniqueIds() {
        val pairCount = 10
        val state = GameFactory.createInitialState(pairCount = pairCount)

        val ids = state.cards.map { it.id }.sorted()
        val expectedIds = (0 until pairCount * 2).toList()

        assertEquals(expectedIds, ids)
    }

    @Test
    fun createInitialState_respectsConfiguration() {
        val pairCount = 6
        val config = ScoringConfig(baseMatchPoints = 500)
        val mode = GameMode.DAILY_CHALLENGE
        val difficulty = DifficultyType.MASTER
        val isHeatShieldAvailable = true

        val state = GameFactory.createInitialState(
            pairCount = pairCount,
            config = config,
            mode = mode,
            difficulty = difficulty,
            isHeatShieldAvailable = isHeatShieldAvailable
        )

        assertEquals(pairCount, state.pairCount)
        assertEquals(config, state.config)
        assertEquals(mode, state.mode)
        assertEquals(difficulty, state.difficulty)
        assertTrue(state.isHeatShieldAvailable)
    }

    @Test
    fun createInitialState_isDeterministic() {
        val seed = 12345
        // We use Random(seed) to create two independent Random instances with the same seed
        val random1 = Random(seed)
        val random2 = Random(seed)

        val state1 = GameFactory.createInitialState(pairCount = 8, random = random1)
        val state2 = GameFactory.createInitialState(pairCount = 8, random = random2)

        assertEquals(state1, state2)
    }
}
