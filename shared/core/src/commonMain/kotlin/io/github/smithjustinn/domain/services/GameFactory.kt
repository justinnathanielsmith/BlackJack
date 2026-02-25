package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.models.Suit
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

/**
 * Domain service responsible for creating initial game states.
 */
object GameFactory {
    /**
     * Creates the initial state for a new memory game.
     */
    fun createInitialState(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
        difficulty: DifficultyType = DifficultyType.CASUAL,
        isHeatShieldAvailable: Boolean = false,
        random: Random = Random,
    ): MemoryGameState {
        val maxPairs = Suit.entries.size * Rank.entries.size
        require(pairCount in 1..maxPairs) { "Pair count must be between 1 and $maxPairs" }

        val allPossibleCards =
            Suit.entries
                .flatMap { suit ->
                    Rank.entries.map { rank -> suit to rank }
                }.shuffled(random)

        val selectedPairs = allPossibleCards.take(pairCount)

        val gameCards =
            selectedPairs
                .flatMap { (suit, rank) ->
                    listOf(
                        CardState(id = 0, suit = suit, rank = rank),
                        CardState(id = 0, suit = suit, rank = rank),
                    )
                }.shuffled(random)
                .mapIndexed { index, card ->
                    card.copy(id = index)
                }.toPersistentList()

        return MemoryGameState(
            cards = gameCards,
            pairCount = pairCount,
            config = config,
            mode = mode,
            difficulty = difficulty,
            isHeatShieldAvailable = isHeatShieldAvailable,
        )
    }
}
