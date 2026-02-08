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
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

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
                }.toPersistentList()

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
        val index = state.cards.indexOfFirst { it.id == cardId }
        val cardToFlip = state.cards.getOrNull(index)

        return when {
            state.isGameOver || cardToFlip == null || cardToFlip.isFaceUp || cardToFlip.isMatched -> state to null
            state.cards.count { it.isFaceUp && !it.isMatched } >= 2 -> state to null
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
        val (first, second) = activeCards.takeIf { it.size == 2 } ?: return state to null

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
        val matchesFound = newCards.count { it.isMatched } / 2
        val isWon = matchesFound == state.pairCount
        val moves = state.moves + 1

        val config = state.config
        val comboFactor = state.comboMultiplier * state.comboMultiplier
        val matchBasePoints = config.baseMatchPoints
        val matchComboBonus = comboFactor * config.comboBonusPoints
        val matchTotalPoints = matchBasePoints + matchComboBonus

        val isMilestone = matchesFound > 0 && matchesFound % config.matchMilestoneInterval == 0
        val potentialPot = state.currentPot + matchTotalPoints

        val scoreResult =
            ScoringCalculator.calculateMatchScore(
                currentScore = if (isMilestone || isWon) state.score + potentialPot else state.score,
                isDoubleDownActive = state.isDoubleDownActive,
                matchBasePoints = 0, // Points are in the pot now
                matchComboBonus = 0,
                isWon = isWon,
            )

        val comment =
            GameCommentGenerator.generateMatchComment(
                moves = moves,
                matchesFound = matchesFound,
                totalPairs = state.pairCount,
                comboMultiplier = state.comboMultiplier,
                config = config,
                isDoubleDownActive = state.isDoubleDownActive,
            )

        val newState =
            state.copy(
                cards = newCards,
                isGameWon = isWon,
                isGameOver = isWon,
                moves = moves,
                score = scoreResult.finalScore,
                currentPot = if (isMilestone || isWon) 0 else potentialPot,
                totalBasePoints = state.totalBasePoints + matchBasePoints,
                totalComboBonus = state.totalComboBonus + matchComboBonus,
                totalDoubleDownBonus = scoreResult.ddBonus,
                comboMultiplier = state.comboMultiplier + 1,
                isDoubleDownActive = state.isDoubleDownActive && !isWon,
                matchComment = comment,
                lastMatchedIds = persistentListOf(first.id, second.id),
            )

        return newState to ScoringCalculator.determineSuccessEvent(isWon, state.comboMultiplier, config)
    }

    private fun handleMatchFailure(
        state: MemoryGameState,
        first: CardState,
        second: CardState,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val errorCards = state.cards.updateByIds(first.id, second.id) { it.copy(isError = true) }

        if (state.isDoubleDownActive) {
            return state.copy(
                score = 0,
                isGameOver = true,
                isGameWon = false,
                isDoubleDownActive = false,
                isBusted = true,
                cards = errorCards,
                lastMatchedIds = persistentListOf(first.id, second.id),
            ) to GameDomainEvent.GameOver
        }

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
        ) to GameDomainEvent.MatchFailure
    }

    fun resetErrorCards(state: MemoryGameState): MemoryGameState {
        var currentCards = state.cards
        var changed = false

        currentCards.forEachIndexed { index, card ->
            if (card.isError) {
                currentCards = currentCards.set(index, card.copy(isFaceUp = false, isError = false))
                changed = true
            }
        }

        return if (changed) state.copy(cards = currentCards) else state
    }

    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = ScoringCalculator.applyFinalBonuses(state, elapsedTimeSeconds)

    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3
}

/**
 * Secondary actions and mutators for the Memory Match game.
 */
object MemoryGameActions {
    private const val MIRAGE_MOVE_INTERVAL = 5

    fun resetErrorCards(state: MemoryGameState): MemoryGameState {
        var currentCards = state.cards
        var changed = false

        currentCards.forEachIndexed { index, card ->
            if (card.isError) {
                currentCards = currentCards.set(index, card.copy(isFaceUp = false, isError = false))
                changed = true
            }
        }

        return if (changed) state.copy(cards = currentCards) else state
    }

    fun activateDoubleDown(state: MemoryGameState): MemoryGameState {
        val unmatchedPairs = state.cards.count { !it.isMatched } / 2
        val isEligible =
            state.comboMultiplier >= state.config.heatModeThreshold &&
                !state.isDoubleDownActive &&
                unmatchedPairs >= MemoryGameLogic.MIN_PAIRS_FOR_DOUBLE_DOWN

        return if (isEligible) state.copy(isDoubleDownActive = true) else state
    }

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
        if (unmatchedIndices.size < 2) return state

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

/**
 * Extension to update multiple cards by their IDs in a single pass if possible,
 * or sequentially for simplicity while maintaining immutability.
 */
private inline fun PersistentList<CardState>.updateByIds(
    vararg ids: Int,
    crossinline transform: (CardState) -> CardState,
): PersistentList<CardState> {
    var result = this
    ids.forEach { id ->
        val index = result.indexOfFirst { it.id == id }
        if (index != -1) {
            result = result.set(index, transform(result[index]))
        }
    }
    return result
}
