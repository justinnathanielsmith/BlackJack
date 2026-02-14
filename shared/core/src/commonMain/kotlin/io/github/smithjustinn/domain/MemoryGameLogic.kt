package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.ScoringCalculator.MatchScoreResult
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
import kotlinx.collections.immutable.mutate
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlin.random.Random

private object GameConstants {
    const val MIRAGE_MOVE_INTERVAL = 5
    const val PAIR_SIZE = 2
    const val SINGLE_CARD = 1
}

/**
 * Pure logic for the Memory Match game.
 */
object MemoryGameLogic {
    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3

    fun createInitialState(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
        difficulty: DifficultyType = DifficultyType.CASUAL,
        isHeatShieldAvailable: Boolean = false,
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
            isHeatShieldAvailable = isHeatShieldAvailable,
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
            state.cards.count { it.isFaceUp && !it.isMatched } >= GameConstants.PAIR_SIZE -> state to null
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
                    GameConstants.SINGLE_CARD -> newState to GameDomainEvent.CardFlipped
                    GameConstants.PAIR_SIZE -> checkForMatch(newState, activeCards)
                    else -> newState to null
                }
            }
        }
    }

    fun resetErrorCards(state: MemoryGameState): MemoryGameState {
        val newCards =
            state.cards.mutate { list ->
                for (index in list.indices) {
                    val card = list[index]
                    if (card.isError) {
                        list[index] = card.copy(isFaceUp = false, isError = false, wasSeen = true)
                    }
                }
            }

        return if (newCards !== state.cards) state.copy(cards = newCards) else state
    }

    fun resetUnmatchedCards(state: MemoryGameState): MemoryGameState {
        var currentCards = state.cards
        var changed = false

        currentCards.forEachIndexed { index, card ->
            if (!card.isMatched && card.isFaceUp) {
                currentCards = currentCards.set(index, card.copy(isFaceUp = false, isError = false, wasSeen = true))
                changed = true
            }
        }

        return if (changed) state.copy(cards = currentCards) else state
    }

    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = ScoringCalculator.applyFinalBonuses(state, elapsedTimeSeconds)

    fun activateDoubleDown(state: MemoryGameState): MemoryGameState {
        val unmatchedPairs = state.cards.count { !it.isMatched } / GameConstants.PAIR_SIZE
        val isEligible =
            state.comboMultiplier >= state.config.heatModeThreshold &&
                !state.isDoubleDownActive &&
                unmatchedPairs >= MIN_PAIRS_FOR_DOUBLE_DOWN

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
                    it.moves % GameConstants.MIRAGE_MOVE_INTERVAL == 0
            }?.let { handleMirageSwap(it, random) } ?: state
}

private fun checkForMatch(
    state: MemoryGameState,
    activeCards: List<CardState>,
): Pair<MemoryGameState, GameDomainEvent?> {
    val (first, second) = activeCards.takeIf { it.size == GameConstants.PAIR_SIZE } ?: return state to null

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
    val matchesFound = newCards.count { it.isMatched } / GameConstants.PAIR_SIZE
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
                ScoringCalculator.calculateMatchScore(
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

    return newState to ScoringCalculator.determineSuccessEvent(isWon, state.comboMultiplier, state.config)
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

    if (state.isHeatShieldAvailable && state.comboMultiplier > 0) {
        return state.copy(
            isHeatShieldAvailable = false,
            moves = state.moves + 1,
            cards = errorCards,
            lastMatchedIds = persistentListOf(),
        ) to GameDomainEvent.HeatShieldUsed
    }

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

private fun handleMirageSwap(
    state: MemoryGameState,
    random: Random,
): MemoryGameState {
    val unmatchedIndices = state.cards.mapIndexedNotNull { index, card -> index.takeUnless { card.isMatched } }
    if (unmatchedIndices.size < GameConstants.PAIR_SIZE) return state

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

// Helper to update multiple cards by their IDs efficiently
private inline fun PersistentList<CardState>.updateByIds(
    vararg ids: Int,
    crossinline transform: (CardState) -> CardState,
): PersistentList<CardState> =
    this.mutate { list ->
        val iterator = list.listIterator()
        while (iterator.hasNext()) {
            val item = iterator.next()
            var found = false
            for (id in ids) {
                if (id == item.id) {
                    found = true
                    break
                }
            }
            if (found) {
                iterator.set(transform(item))
            }
        }
    }

/**
 * Secondary actions and mutators for the Memory Match game.
 * Restored for backward compatibility.
 */
object MemoryGameActions {
    fun resetErrorCards(state: MemoryGameState): MemoryGameState = MemoryGameLogic.resetErrorCards(state)

    fun resetUnmatchedCards(state: MemoryGameState): MemoryGameState = MemoryGameLogic.resetUnmatchedCards(state)

    fun activateDoubleDown(state: MemoryGameState): MemoryGameState = MemoryGameLogic.activateDoubleDown(state)

    fun applyMutators(
        state: MemoryGameState,
        random: Random = Random,
    ): MemoryGameState = MemoryGameLogic.applyMutators(state, random)
}

private data class MatchScoringUpdate(
    val matchBasePoints: Int,
    val matchComboBonus: Int,
    val potentialPot: Long,
    val isMilestone: Boolean,
    val scoreResult: MatchScoreResult,
)
