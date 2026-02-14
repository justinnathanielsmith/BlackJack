package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.collections.immutable.toPersistentList
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
        if (unmatchedIndices.size < PAIR_SIZE) return state

        val (idx1, idx2) = unmatchedIndices.shuffled(random)
        val newCards =
            state.cards
                .toMutableList()
                .apply {
                    val temp = this[idx1]
                    this[idx1] = this[idx2]
                    this[idx2] = temp
                }.toPersistentList()

        return state.copy(cards = newCards)
    }
}
