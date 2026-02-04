package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.models.Suit
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random
import kotlinx.collections.immutable.ImmutableList

/**
 * Pure logic for the Memory Match game.
 */
object MemoryGameLogic {
    fun createInitialState(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
        difficulty: DifficultyType = DifficultyType.CASUAL,
        random: Random = Random,
    ): MemoryGameState {
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
                }.toImmutableList()

        return MemoryGameState(
            cards = gameCards,
            pairCount = pairCount,
            config = config,
            mode = mode,
            difficulty = difficulty,
        )
    }

    fun flipCard(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        // Optimization: avoid O(N) allocation with map.
        // Find index first (O(N) scan but no allocation) then set (O(log N)).
        val index = state.cards.indexOfFirst { it.id == cardId }
        if (index == -1) {
            return state to null
        }

        val cardToFlip = state.cards[index]
        val faceUpCards = state.cards.filter { it.isFaceUp && !it.isMatched }

        return when {
            state.isGameOver -> {
                state to null
            }

            cardToFlip.isFaceUp || cardToFlip.isMatched -> {
                state to null
            }

            faceUpCards.size >= 2 -> {
                state to null
            }

            else -> {
                val newCards =
                    if (state.cards is PersistentList) {
                        state.cards.set(index, cardToFlip.copy(isFaceUp = true))
                    } else {
                        state.cards.toPersistentList().set(index, cardToFlip.copy(isFaceUp = true))
                    }

                val newState =
                    state.copy(
                        cards = newCards,
                        // Clear last matched IDs when starting a new turn
                        lastMatchedIds = if (faceUpCards.isEmpty()) persistentListOf() else state.lastMatchedIds,
                    )

                val activeCards = newState.cards.filter { it.isFaceUp && !it.isMatched }
                when (activeCards.size) {
                    1 -> newState to GameDomainEvent.CardFlipped
                    2 -> checkForMatch(newState, activeCards)
                    else -> newState to null
                }
            }
        }
    }

    private fun checkForMatch(
        state: MemoryGameState,
        activeCards: List<CardState>,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        if (activeCards.size != 2) return state to null

        val first = activeCards[0]
        val second = activeCards[1]

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
        val newCards = updateCardsForMatch(state.cards, first.id, second.id)

        val matchesFound = newCards.count { it.isMatched } / 2
        val isWon = matchesFound == state.pairCount
        val moves = state.moves + 1

        val config = state.config
        val comboFactor = state.comboMultiplier * state.comboMultiplier
        val matchBasePoints = config.baseMatchPoints
        val matchComboBonus = comboFactor * config.comboBonusPoints

        val scoreResult =
            ScoringCalculator.calculateMatchScore(
                currentScore = state.score,
                isDoubleDownActive = state.isDoubleDownActive,
                matchBasePoints = matchBasePoints,
                matchComboBonus = matchComboBonus,
                isWon = isWon,
            )

        val comment =
            GameCommentGenerator.generateMatchComment(
                moves,
                matchesFound,
                state.pairCount,
                state.comboMultiplier,
                config,
                isDoubleDownActive = state.isDoubleDownActive,
            )

        val newState =
            state.copy(
                cards = newCards,
                isGameWon = isWon,
                isGameOver = isWon,
                moves = moves,
                score = scoreResult.finalScore,
                totalBasePoints = state.totalBasePoints + matchBasePoints,
                totalComboBonus = state.totalComboBonus + matchComboBonus,
                totalDoubleDownBonus = scoreResult.ddBonus,
                comboMultiplier = state.comboMultiplier + 1,
                isDoubleDownActive = state.isDoubleDownActive && !isWon, // Deactivate on win
                matchComment = comment,
                lastMatchedIds = persistentListOf(first.id, second.id),
            )

        val event = ScoringCalculator.determineSuccessEvent(isWon, state.comboMultiplier, config)

        return newState to event
    }

    private fun handleMatchFailure(
        state: MemoryGameState,
        first: CardState,
        second: CardState,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val errorCards = updateCardsForError(state.cards, first.id, second.id)

        // All-In Rule: Mismatch while Double Down is active results in Game Over and 0 Score
        if (state.isDoubleDownActive) {
            return state.copy(
                score = 0,
                isGameOver = true,
                isGameWon = false,
                isDoubleDownActive = false,
                isBusted = true, // BUSTED!
                cards = errorCards,
                lastMatchedIds = persistentListOf(first.id, second.id), // Show the error
            ) to GameDomainEvent.GameOver
        }

        val newState =
            state.copy(
                moves = state.moves + 1,
                comboMultiplier = 0,
                score = state.score.coerceAtLeast(0),
                isGameOver = false,
                isDoubleDownActive = false,
                cards = errorCards,
                lastMatchedIds = persistentListOf(),
            )
        return newState to GameDomainEvent.MatchFailure
    }

    fun resetErrorCards(state: MemoryGameState): MemoryGameState {
        var newCards = if (state.cards is PersistentList) state.cards else state.cards.toPersistentList()
        var changed = false

        for (i in newCards.indices) {
            if (newCards[i].isError) {
                newCards = newCards.set(i, newCards[i].copy(isFaceUp = false, isError = false))
                changed = true
            }
        }

        return if (changed) state.copy(cards = newCards) else state
    }

    /**
     * Calculates and applies bonuses to the final score when the game is won.
     * The move efficiency is now the dominant factor.
     */
    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = ScoringCalculator.applyFinalBonuses(state, elapsedTimeSeconds)

    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3
    private const val MIRAGE_MOVE_INTERVAL = 5

    /**
     * Activates Double Down if requirements are met.
     */
    fun activateDoubleDown(state: MemoryGameState): MemoryGameState {
        val unmatchedPairs = state.cards.count { !it.isMatched } / 2
        val isEligible =
            state.comboMultiplier >= state.config.heatModeThreshold &&
                !state.isDoubleDownActive &&
                unmatchedPairs >= MIN_PAIRS_FOR_DOUBLE_DOWN

        return if (isEligible) {
            state.copy(isDoubleDownActive = true)
        } else {
            state
        }
    }

    /**
     * Applies active mutators to the game state.
     * Currently handles:
     * - MIRAGE: Swaps two random unmatched cards every 5 moves.
     */
    fun applyMutators(
        state: MemoryGameState,
        random: Random = Random,
    ): MemoryGameState {
        var currentState = state
        if (state.activeMutators.contains(DailyChallengeMutator.MIRAGE) &&
            state.moves > 0 &&
            state.moves % MIRAGE_MOVE_INTERVAL == 0
        ) {
            currentState = handleMirageSwap(currentState, random)
        }
        return currentState
    }

    private fun handleMirageSwap(
        state: MemoryGameState,
        random: Random,
    ): MemoryGameState {
        val unmatchedIndices =
            state.cards
                .mapIndexedNotNull { index, card ->
                    if (!card.isMatched) index else null
                }

        if (unmatchedIndices.size < 2) return state

        val shuffledIndices = unmatchedIndices.shuffled(random)
        val idx1 = shuffledIndices[0]
        val idx2 = shuffledIndices[1]

        val newCards = state.cards.toMutableList()
        val temp = newCards[idx1]
        newCards[idx1] = newCards[idx2]
        newCards[idx2] = temp

        return state.copy(cards = newCards.toImmutableList())
    }
}

private fun updateCardsForMatch(
    cards: ImmutableList<CardState>,
    firstId: Int,
    secondId: Int,
): ImmutableList<CardState> {
    val persistentCards = if (cards is PersistentList) cards else cards.toPersistentList()
    val firstIndex = persistentCards.indexOfFirst { it.id == firstId }
    val secondIndex = persistentCards.indexOfFirst { it.id == secondId }

    var result = persistentCards
    if (firstIndex != -1) {
        result = result.set(firstIndex, result[firstIndex].copy(isMatched = true, isFaceUp = true))
    }
    if (secondIndex != -1) {
        result = result.set(secondIndex, result[secondIndex].copy(isMatched = true, isFaceUp = true))
    }
    return result
}

private fun updateCardsForError(
    cards: ImmutableList<CardState>,
    firstId: Int,
    secondId: Int,
): ImmutableList<CardState> {
    val persistentCards = if (cards is PersistentList) cards else cards.toPersistentList()
    val firstIndex = persistentCards.indexOfFirst { it.id == firstId }
    val secondIndex = persistentCards.indexOfFirst { it.id == secondId }

    var result = persistentCards
    if (firstIndex != -1) {
        result = result.set(firstIndex, result[firstIndex].copy(isError = true))
    }
    if (secondIndex != -1) {
        result = result.set(secondIndex, result[secondIndex].copy(isError = true))
    }
    return result
}
