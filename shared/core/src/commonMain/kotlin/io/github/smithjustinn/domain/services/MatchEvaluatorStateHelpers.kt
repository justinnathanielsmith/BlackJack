package io.github.smithjustinn.domain.services

import io.github.smithjustinn.domain.GameCommentGenerator
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.MatchScoringUpdate
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf

internal fun createMatchSuccessState(
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
        currentPot =
            if (scoringUpdate.isMilestone || isWon) 0 else scoringUpdate.potentialPot.toInt(),
        totalBasePoints = state.totalBasePoints + scoringUpdate.matchBasePoints,
        totalComboBonus = state.totalComboBonus + scoringUpdate.matchComboBonus,
        totalDoubleDownBonus = scoringUpdate.scoreResult.ddBonus,
        comboMultiplier = state.comboMultiplier + 1,
        isDoubleDownActive = state.isDoubleDownActive && !isWon,
        matchComment = comment,
        lastMatchedIds = persistentListOf(firstId, secondId),
    )
}

internal fun createHeatShieldUsedState(
    state: MemoryGameState,
    errorCards: PersistentList<CardState>,
): MemoryGameState =
    state.copy(
        isHeatShieldAvailable = false,
        moves = state.moves + 1,
        cards = errorCards,
        lastMatchedIds = persistentListOf(),
    )

internal fun createBustedState(
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

internal fun createStandardFailureState(
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

// Helper to update multiple cards by their IDs efficiently
internal inline fun PersistentList<CardState>.updateByIds(
    vararg ids: Int,
    crossinline transform: (CardState) -> CardState,
): PersistentList<CardState> =
    this.mutate { list ->
        for (id in ids) {
            // Bolt optimization: IDs match indices in standard games
            if (id in list.indices && list[id].id == id) {
                list[id] = transform(list[id])
            } else {
                val index = list.indexOfFirst { it.id == id }
                if (index != -1) {
                    list[index] = transform(list[index])
                }
            }
        }
    }
