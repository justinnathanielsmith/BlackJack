package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.GameCommentGenerator
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.MatchScoreResult
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

private object MatchConstants {
    const val PAIR_SIZE = 2
    const val SINGLE_CARD = 1
    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3
}

/**
 * Domain service responsible for card flipping and match evaluation logic.
 */
object MatchEvaluator {
    /**
     * Attempts to flip a card and returns the new state and any resulting event.
     */
    fun flipCard(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val index = state.cards.indexOfFirst { it.id == cardId }
        val cardToFlip = state.cards.getOrNull(index)

        return when {
            state.isGameOver || cardToFlip == null || cardToFlip.isFaceUp || cardToFlip.isMatched -> state to null
            state.cards.count { it.isFaceUp && !it.isMatched } >= MatchConstants.PAIR_SIZE -> state to null
            else -> {
                val newCards = state.cards.set(index, cardToFlip.copy(isFaceUp = true))
                val faceUpCountBefore = state.cards.count { it.isFaceUp && !it.isMatched }
                val newState =
                    state.copy(
                        cards = newCards,
                        lastMatchedIds = if (faceUpCountBefore == 0) persistentListOf() else state.lastMatchedIds,
                    )

                val activeCards = newState.cards.filter { it.isFaceUp && !it.isMatched }
                when (activeCards.size) {
                    MatchConstants.SINGLE_CARD -> newState to GameDomainEvent.CardFlipped
                    MatchConstants.PAIR_SIZE -> checkForMatch(newState, activeCards)
                    else -> newState to null
                }
            }
        }
    }

    /**
     * Resets all error cards back to face-down and clears the error flag.
     */
    fun resetErrorCards(state: MemoryGameState): MemoryGameState {
        val newCards =
            state.cards.mutate { list ->
                val iterator = list.listIterator()
                while (iterator.hasNext()) {
                    val card = iterator.next()
                    if (card.isError) {
                        iterator.set(card.copy(isFaceUp = false, isError = false, wasSeen = true))
                    }
                }
            }

        return if (newCards !== state.cards) state.copy(cards = newCards) else state
    }

    /**
     * Resets all unmatched cards back to face-down.
     */
    fun resetUnmatchedCards(state: MemoryGameState): MemoryGameState {
        val newCards =
            state.cards.mutate { list ->
                val iterator = list.listIterator()
                while (iterator.hasNext()) {
                    val card = iterator.next()
                    if (!card.isMatched && card.isFaceUp) {
                        iterator.set(card.copy(isFaceUp = false, isError = false, wasSeen = true))
                    }
                }
            }

        return if (newCards !== state.cards) state.copy(cards = newCards) else state
    }

    /**
     * Activates Double Down mode if eligible.
     */
    fun activateDoubleDown(state: MemoryGameState): MemoryGameState {
        val unmatchedPairs = state.cards.count { !it.isMatched } / MatchConstants.PAIR_SIZE
        val isEligible =
            state.comboMultiplier >= state.config.heatModeThreshold &&
                !state.isDoubleDownActive &&
                unmatchedPairs >= MatchConstants.MIN_PAIRS_FOR_DOUBLE_DOWN

        return if (isEligible) state.copy(isDoubleDownActive = true) else state
    }

    private fun checkForMatch(
        state: MemoryGameState,
        activeCards: List<CardState>,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val (first, second) = activeCards.takeIf { it.size == MatchConstants.PAIR_SIZE } ?: return state to null

        return if (first.suit == second.suit && first.rank == second.rank) {
            handleMatchSuccess(state, first, second)
        } else {
            handleMatchFailure(state, first, second)
        }
    }

    private fun handleMatchSuccess(
        state: MemoryGameState,
        first: CardState,
        second: CardState,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val newCards = state.cards.updateByIds(first.id, second.id) { it.copy(isMatched = true, isFaceUp = true) }
        val matchesFound = newCards.count { it.isMatched } / MatchConstants.PAIR_SIZE
        val isWon = matchesFound == state.pairCount
        val moves = state.moves + 1

        val comboFactor = state.comboMultiplier * state.comboMultiplier
        val matchBasePoints = state.config.baseMatchPoints
        val matchComboBonus = comboFactor * state.config.comboBonusPoints
        val matchTotalPoints = (matchBasePoints + matchComboBonus).toLong()

        val isMilestone = matchesFound > 0 && matchesFound % state.config.matchMilestoneInterval == 0
        val potentialPot = state.currentPot + matchTotalPoints

        val scoringUpdate =
            MatchScoringUpdate(
                matchBasePoints = matchBasePoints,
                matchComboBonus = matchComboBonus,
                potentialPot = potentialPot,
                isMilestone = isMilestone,
                scoreResult =
                    ScoreKeeper.calculateMatchScore(
                        currentScore = if (isMilestone || isWon) state.score + potentialPot.toInt() else state.score,
                        isDoubleDownActive = state.isDoubleDownActive,
                        matchBasePoints = 0,
                        matchComboBonus = 0,
                        isWon = isWon,
                    ),
            )

        val newState =
            createMatchSuccessState(
                state = state,
                newCards = newCards,
                isWon = isWon,
                moves = moves,
                scoringUpdate = scoringUpdate,
                matchesFound = matchesFound,
                firstId = first.id,
                secondId = second.id,
            )

        return newState to ScoreKeeper.determineSuccessEvent(isWon, state.comboMultiplier, state.config)
    }

    private fun createMatchSuccessState(
        state: MemoryGameState,
        newCards: PersistentList<CardState>,
        isWon: Boolean,
        moves: Int,
        scoringUpdate: MatchScoringUpdate,
        matchesFound: Int,
        firstId: Int,
        secondId: Int,
    ): MemoryGameState {
        val comment =
            GameCommentGenerator.generateMatchComment(
                moves = moves,
                matchesFound = matchesFound,
                totalPairs = state.pairCount,
                comboMultiplier = state.comboMultiplier,
                config = state.config,
                isDoubleDownActive = state.isDoubleDownActive,
            )

        return state.copy(
            cards = newCards,
            isGameWon = isWon,
            isGameOver = isWon,
            moves = moves,
            score = scoringUpdate.scoreResult.finalScore,
            currentPot = if (scoringUpdate.isMilestone || isWon) 0 else scoringUpdate.potentialPot.toInt(),
            totalBasePoints = state.totalBasePoints + scoringUpdate.matchBasePoints,
            totalComboBonus = state.totalComboBonus + scoringUpdate.matchComboBonus,
            totalDoubleDownBonus = scoringUpdate.scoreResult.ddBonus,
            comboMultiplier = state.comboMultiplier + 1,
            isDoubleDownActive = state.isDoubleDownActive && !isWon,
            matchComment = comment,
            lastMatchedIds = persistentListOf(firstId, secondId),
        )
    }

    private fun handleMatchFailure(
        state: MemoryGameState,
        first: CardState,
        second: CardState,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val errorCards = state.cards.updateByIds(first.id, second.id) { it.copy(isError = true) }

        return when {
            state.isHeatShieldAvailable && state.comboMultiplier > 0 -> {
                createHeatShieldUsedState(state, errorCards) to GameDomainEvent.HeatShieldUsed
            }

            state.isDoubleDownActive -> {
                createBustedState(state, errorCards, first.id, second.id) to GameDomainEvent.GameOver
            }

            else -> {
                createStandardFailureState(state, errorCards) to GameDomainEvent.MatchFailure
            }
        }
    }

    private fun createHeatShieldUsedState(
        state: MemoryGameState,
        errorCards: PersistentList<CardState>,
    ): MemoryGameState =
        state.copy(
            isHeatShieldAvailable = false,
            moves = state.moves + 1,
            cards = errorCards,
            lastMatchedIds = persistentListOf(),
        )

    private fun createBustedState(
        state: MemoryGameState,
        errorCards: PersistentList<CardState>,
        firstId: Int,
        secondId: Int,
    ): MemoryGameState =
        state.copy(
            score = 0,
            isGameOver = true,
            isGameWon = false,
            isDoubleDownActive = false,
            isBusted = true,
            cards = errorCards,
            lastMatchedIds = persistentListOf(firstId, secondId),
        )

    private fun createStandardFailureState(
        state: MemoryGameState,
        errorCards: PersistentList<CardState>,
    ): MemoryGameState {
        val penalty = (state.currentPot * state.config.potMismatchPenalty).toInt()
        val newPot = (state.currentPot - penalty).coerceAtLeast(0)

        return state.copy(
            moves = state.moves + 1,
            comboMultiplier = 0,
            score = state.score.coerceAtLeast(0),
            currentPot = newPot,
            isGameOver = false,
            isDoubleDownActive = false,
            cards = errorCards,
            lastMatchedIds = persistentListOf(),
        )
    }
}

// Helper to update multiple cards by their IDs efficiently
private inline fun PersistentList<CardState>.updateByIds(
    vararg ids: Int,
    crossinline transform: (CardState) -> CardState,
): PersistentList<CardState> =
    this.mutate { list ->
        val iterator = list.listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            if (item.id in ids) {
                iterator.set(transform(item))
            }
        }
    }

private data class MatchScoringUpdate(
    val matchBasePoints: Int,
    val matchComboBonus: Int,
    val potentialPot: Long,
    val isMilestone: Boolean,
    val scoreResult: MatchScoreResult,
)
