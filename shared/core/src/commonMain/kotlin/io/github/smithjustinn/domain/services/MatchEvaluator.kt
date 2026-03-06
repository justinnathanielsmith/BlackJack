package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

private object MatchConstants {
    const val PAIR_SIZE = 2
    const val SINGLE_CARD = 1
}

/**
 * Domain service responsible for card flipping and match evaluation logic.
 */
object MatchEvaluator {
    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3

    /**
     * Attempts to flip a card and returns the new state and any resulting event.
     */
    fun flipCard(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val cardAtIndex = state.cards.getOrNull(cardId)
        val index =
            if (cardAtIndex?.id == cardId) cardId else state.cards.indexOfFirst { it.id == cardId }
        val cardToFlip = state.cards.getOrNull(index)

        return if (canFlip(state, cardToFlip)) {
            val newState = applyFlip(state, index, cardToFlip!!)
            val activeCards = findActiveCards(newState.cards)
            evaluateMatch(newState, activeCards)
        } else {
            state to null
        }
    }

    /**
     * Resets all error cards back to face-down and clears the error flag.
     */
    fun resetErrorCards(state: MemoryGameState): MemoryGameState {
        val newCards =
            state.cards.mutate { list ->
                for (i in list.indices) {
                    val card = list[i]
                    if (card.isError) {
                        list[i] = card.copy(isFaceUp = false, isError = false, wasSeen = true)
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
                for (i in list.indices) {
                    val card = list[i]
                    if (!card.isMatched && card.isFaceUp) {
                        list[i] = card.copy(isFaceUp = false, isError = false, wasSeen = true)
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
                unmatchedPairs >= MIN_PAIRS_FOR_DOUBLE_DOWN

        return if (isEligible) state.copy(isDoubleDownActive = true) else state
    }
}

private fun canFlip(
    state: MemoryGameState,
    card: CardState?,
): Boolean {
    val isGameOverOrInvalidCard = state.isGameOver || card == null || card.isFaceUp || card.isMatched
    return if (isGameOverOrInvalidCard) {
        false
    } else {
        // Bolt optimization: Count active cards without full iteration if possible
        countActiveCardsAndCheckIfCanFlip(state)
    }
}

private fun countActiveCardsAndCheckIfCanFlip(state: MemoryGameState): Boolean {
    var faceUpCount = 0
    var canStillFlip = true
    for (c in state.cards) {
        if (c.isFaceUp && !c.isMatched) {
            faceUpCount++
            if (faceUpCount >= MatchConstants.PAIR_SIZE) {
                canStillFlip = false
                break
            }
        }
    }
    return canStillFlip && faceUpCount < MatchConstants.PAIR_SIZE
}

private fun checkForMatch(
    state: MemoryGameState,
    activeCards: List<CardState>,
): Pair<MemoryGameState, GameDomainEvent?> {
    val (first, second) =
        activeCards.takeIf { it.size == MatchConstants.PAIR_SIZE } ?: return state to null

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
    val newCards =
        state.cards.updateByIds(first.id, second.id) {
            it.copy(isMatched = true, isFaceUp = true)
        }
    val matchesFound = newCards.count { it.isMatched } / MatchConstants.PAIR_SIZE
    val isWon = matchesFound == state.pairCount
    val moves = state.moves + 1

    val scoringUpdate =
        ScoreKeeper.calculateMatchUpdate(
            state = state,
            isWon = isWon,
            matchesFound = matchesFound,
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

    return newState to
        ScoreKeeper.determineSuccessEvent(isWon, state.comboMultiplier, state.config)
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
            createBustedState(state, errorCards, first.id, second.id) to
                GameDomainEvent.GameOver
        }
        else -> {
            createStandardFailureState(state, errorCards) to GameDomainEvent.MatchFailure
        }
    }
}

private fun applyFlip(
    state: MemoryGameState,
    index: Int,
    card: CardState,
): MemoryGameState {
    val newCards = state.cards.set(index, card.copy(isFaceUp = true))
    val faceUpCountBefore = state.cards.count { it.isFaceUp && !it.isMatched }
    return state.copy(
        cards = newCards,
        lastMatchedIds =
            if (faceUpCountBefore == 0) persistentListOf() else state.lastMatchedIds,
    )
}

private fun evaluateMatch(
    state: MemoryGameState,
    activeCards: List<CardState>,
): Pair<MemoryGameState, GameDomainEvent?> =
    when (activeCards.size) {
        MatchConstants.SINGLE_CARD -> state to GameDomainEvent.CardFlipped
        MatchConstants.PAIR_SIZE -> checkForMatch(state, activeCards)
        else -> state to null
    }

private fun findActiveCards(cards: List<CardState>): List<CardState> {
    val activeCards = mutableListOf<CardState>()
    for (i in 0 until cards.size) {
        val c = cards[i]
        if (c.isFaceUp && !c.isMatched) {
            activeCards.add(c)
            if (activeCards.size == MatchConstants.PAIR_SIZE) break
        }
    }
    return activeCards
}
