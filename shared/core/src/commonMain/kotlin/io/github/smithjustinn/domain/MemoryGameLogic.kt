package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.models.Suit
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlin.random.Random

/**
 * Pure logic for the Memory Match game.
 */
object MemoryGameLogic {
    fun createInitialState(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
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
        )
    }

    fun flipCard(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val cardToFlip = state.cards.find { it.id == cardId }
        val faceUpCards = state.cards.filter { it.isFaceUp && !it.isMatched }

        return when {
            state.isGameOver -> {
                state to null
            }

            cardToFlip == null || cardToFlip.isFaceUp || cardToFlip.isMatched -> {
                state to null
            }

            faceUpCards.size >= 2 -> {
                state to null
            }

            else -> {
                val newState =
                    state.copy(
                        cards =
                            state.cards
                                .map { card ->
                                    if (card.id == cardId) card.copy(isFaceUp = true) else card
                                }.toImmutableList(),
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

        val scoreResult = calculateMatchScore(state, isWon, matchBasePoints, matchComboBonus)

        val comment =
            GameCommentGenerator.generateMatchComment(
                moves,
                matchesFound,
                state.pairCount,
                state.comboMultiplier,
                config,
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

        val event = determineSuccessEvent(isWon, state.comboMultiplier, config)

        return newState to event
    }

    private fun handleMatchFailure(
        state: MemoryGameState,
        first: CardState,
        second: CardState,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        // All-In Rule: Mismatch while Double Down is active results in Game Over and 0 Score
        if (state.isDoubleDownActive) {
            return state.copy(
                score = 0,
                isGameOver = true,
                isGameWon = false,
                isDoubleDownActive = false,
                isBusted = true, // BUSTED!
                cards =
                    state.cards
                        .map { card ->
                            if (card.id == first.id || card.id == second.id) {
                                card.copy(isError = true)
                            } else {
                                card
                            }
                        }.toImmutableList(),
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
                cards =
                    state.cards
                        .map { card ->
                            if (card.id == first.id || card.id == second.id) {
                                card.copy(isError = true)
                            } else {
                                card
                            }
                        }.toImmutableList(),
                lastMatchedIds = persistentListOf(),
            )
        return newState to GameDomainEvent.MatchFailure
    }

    fun resetErrorCards(state: MemoryGameState): MemoryGameState =
        state.copy(
            cards =
                state.cards
                    .map { card ->
                        if (card.isError) card.copy(isFaceUp = false, isError = false) else card
                    }.toImmutableList(),
        )

    /**
     * Calculates and applies bonuses to the final score when the game is won.
     * The move efficiency is now the dominant factor.
     */
    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState {
        if (!state.isGameWon) return state

        val config = state.config

        // Time Bonus: Small impact
        val timeBonus =
            if (state.mode == GameMode.TIME_ATTACK) {
                // In Time Attack, remaining time is the bonus
                (elapsedTimeSeconds * TIME_ATTACK_BONUS_MULTIPLIER).toInt() // Example: 10 points per remaining second
            } else {
                (state.pairCount * config.timeBonusPerPair - (elapsedTimeSeconds * config.timePenaltyPerSecond))
                    .coerceAtLeast(0)
                    .toInt()
            }

        // Move Efficiency Bonus: Dominant factor
        val moveEfficiency = state.pairCount.toDouble() / state.moves.toDouble()
        val moveBonus = (moveEfficiency * config.moveBonusMultiplier).toInt()

        val totalScore = state.score + timeBonus + moveBonus

        return state.copy(
            score = totalScore,
            scoreBreakdown =
                ScoreBreakdown(
                    basePoints = state.totalBasePoints,
                    comboBonus = state.totalComboBonus,
                    doubleDownBonus = state.totalDoubleDownBonus,
                    timeBonus = timeBonus,
                    moveBonus = moveBonus,
                    totalScore = totalScore,
                ),
        )
    }

    private const val TIME_ATTACK_BONUS_MULTIPLIER = 10
    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3

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
}

private fun updateCardsForMatch(
    cards: List<CardState>,
    firstId: Int,
    secondId: Int,
): kotlinx.collections.immutable.ImmutableList<CardState> =
    cards
        .map { card ->
            if (card.id == firstId || card.id == secondId) {
                card.copy(isMatched = true, isFaceUp = true)
            } else {
                card
            }
        }.toImmutableList()

private fun calculateMatchScore(
    state: MemoryGameState,
    isWon: Boolean,
    matchBasePoints: Int,
    matchComboBonus: Int,
): MatchScoreResult {
    val matchPoints = matchBasePoints + matchComboBonus

    return if (isWon && state.isDoubleDownActive) {
        val totalWithoutBonus = state.score + matchPoints
        val finalScore = totalWithoutBonus * 2
        MatchScoreResult(
            finalScore = finalScore,
            ddBonus = finalScore - totalWithoutBonus,
        )
    } else {
        val multiplier = if (state.isDoubleDownActive) 2 else 1
        val matchTotal = matchPoints * multiplier
        MatchScoreResult(
            finalScore = state.score + matchTotal,
            ddBonus = if (state.isDoubleDownActive) matchTotal / 2 else 0,
        )
    }
}

private data class MatchScoreResult(
    val finalScore: Int,
    val ddBonus: Int,
)

private fun determineSuccessEvent(
    isWon: Boolean,
    comboMultiplier: Int,
    config: ScoringConfig,
): GameDomainEvent =
    when {
        isWon -> GameDomainEvent.GameWon
        comboMultiplier > config.theNutsThreshold -> GameDomainEvent.TheNutsAchieved
        else -> GameDomainEvent.MatchSuccess
    }
