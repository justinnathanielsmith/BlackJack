package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.collections.immutable.mutate
import kotlin.random.Random

/**
 * Domain service responsible for applying Daily Challenge mutators.
 */
object MutatorEngine {
    private const val MIRAGE_MOVE_INTERVAL = 5
    private const val PAIR_SIZE = 2

    /**
     * Applies active mutators to the game state.
     * Currently handles MIRAGE mutator which swaps unmatched cards every 5 moves.
     */
    fun applyMutators(
        state: MemoryGameState,
        random: Random = Random,
    ): MemoryGameState =
        state
            .takeIf {
                it.activeMutators.contains(DailyChallengeMutator.MIRAGE) &&
                    it.moves > 0 &&
                    it.moves % MIRAGE_MOVE_INTERVAL == 0
            }?.let { handleMirageSwap(it, random) } ?: state

    private fun handleMirageSwap(
        state: MemoryGameState,
        random: Random,
    ): MemoryGameState {
        val unmatchedIndices = state.cards.mapIndexedNotNull { index, card -> index.takeUnless { card.isMatched } }
        val size = unmatchedIndices.size
        if (size < PAIR_SIZE) return state

        // Pick two distinct random indices
        val i1 = random.nextInt(size)
        // Adjust second index to ensure it's distinct from the first
        val i2 = random.nextInt(size - 1).let { if (it >= i1) it + 1 else it }

        val idx1 = unmatchedIndices[i1]
        val idx2 = unmatchedIndices[i2]

        val newCards =
            state.cards.mutate { list ->
                val temp = list[idx1]
                list[idx1] = list[idx2]
                list[idx2] = temp
            }

        return state.copy(cards = newCards)
    }
}
