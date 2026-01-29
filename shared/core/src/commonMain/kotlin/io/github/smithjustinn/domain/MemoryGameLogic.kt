package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.comment_all_in
import io.github.smithjustinn.resources.comment_bad_beat
import io.github.smithjustinn.resources.comment_boom
import io.github.smithjustinn.resources.comment_eagle_eyes
import io.github.smithjustinn.resources.comment_full_house
import io.github.smithjustinn.resources.comment_great_find
import io.github.smithjustinn.resources.comment_high_roller
import io.github.smithjustinn.resources.comment_on_a_roll
import io.github.smithjustinn.resources.comment_one_more
import io.github.smithjustinn.resources.comment_perfect
import io.github.smithjustinn.resources.comment_photographic
import io.github.smithjustinn.resources.comment_pot_odds
import io.github.smithjustinn.resources.comment_sharp
import io.github.smithjustinn.resources.comment_the_nuts
import io.github.smithjustinn.resources.comment_you_got_it
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
        mode: GameMode = GameMode.STANDARD,
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
        val newCards =
            state.cards
                .map { card ->
                    if (card.id == first.id || card.id == second.id) {
                        card.copy(isMatched = true)
                    } else {
                        card
                    }
                }.toImmutableList()

        val config = state.config
        val comboFactor = state.comboMultiplier * state.comboMultiplier
        val matchBasePoints = config.baseMatchPoints
        val matchComboBonus = comboFactor * config.comboBonusPoints
        val matchPoints = matchBasePoints + matchComboBonus
        val pointsEarned = matchPoints

        val matchesFound = newCards.count { it.isMatched } / 2
        val isWon = matchesFound == state.pairCount
        val moves = state.moves + 1

        val comment = generateMatchComment(moves, matchesFound, state.pairCount, state.comboMultiplier, config)

        val preDoubleScore = state.score + pointsEarned
        val finalScore = if (isWon && state.isDoubleDownActive) {
            preDoubleScore * 2
        } else {
            preDoubleScore
        }
        val ddBonus = finalScore - preDoubleScore
        
        val newState =
            state.copy(
                cards = newCards,
                isGameWon = isWon,
                isGameOver = isWon,
                moves = moves,
                score = finalScore,
                totalBasePoints = state.totalBasePoints + matchBasePoints,
                totalComboBonus = state.totalComboBonus + matchComboBonus,
                totalDoubleDownBonus = ddBonus,
                comboMultiplier = state.comboMultiplier + 1,
                isDoubleDownActive = state.isDoubleDownActive && !isWon, // Deactivate on win
                matchComment = comment,
                lastMatchedIds = persistentListOf(first.id, second.id),
            )

        val event =
            when {
                isWon -> GameDomainEvent.GameWon
                state.comboMultiplier > config.theNutsThreshold -> GameDomainEvent.TheNutsAchieved
                else -> GameDomainEvent.MatchSuccess
            }

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

    private fun generateMatchComment(
        moves: Int,
        matchesFound: Int,
        totalPairs: Int,
        combo: Int,
        config: ScoringConfig,
    ): MatchComment {
        if (matchesFound == totalPairs) return MatchComment(Res.string.comment_perfect)

        return when {
            combo > config.theNutsThreshold -> {
                MatchComment(Res.string.comment_the_nuts, persistentListOf(combo))
            }

            combo > config.highRollerThreshold -> {
                MatchComment(Res.string.comment_high_roller, persistentListOf(combo))
            }

            matchesFound == 1 -> {
                MatchComment(Res.string.comment_all_in)
            }

            matchesFound == totalPairs / 2 -> {
                MatchComment(Res.string.comment_pot_odds)
            }

            moves <= matchesFound * 2 -> {
                MatchComment(Res.string.comment_photographic)
            }

            matchesFound == totalPairs - 1 -> {
                MatchComment(Res.string.comment_one_more)
            }

            else -> {
                val randomRes =
                    listOf(
                        Res.string.comment_great_find,
                        Res.string.comment_you_got_it,
                        Res.string.comment_boom,
                        Res.string.comment_eagle_eyes,
                        Res.string.comment_sharp,
                        Res.string.comment_on_a_roll,
                        Res.string.comment_full_house, // Poker Term
                        Res.string.comment_bad_beat, // Poker Term (Irony)
                    ).random()
                MatchComment(randomRes)
            }
        }
    }

    // --- Time Attack Logic ---

    fun calculateInitialTime(pairCount: Int): Long =
        when (pairCount) {
            DIFF_LEVEL_6 -> INITIAL_TIME_6

            // Tourist
            DIFF_LEVEL_8 -> INITIAL_TIME_8

            // Casual
            DIFF_LEVEL_10 -> INITIAL_TIME_10

            // Master
            DIFF_LEVEL_12 -> INITIAL_TIME_12

            // Shark
            else -> (pairCount * TIME_PER_PAIR_FALLBACK).toLong()
        }

    fun calculateTimeGain(comboMultiplier: Int): Int {
        val baseGain = BASE_TIME_GAIN
        // Steeper time reward: +2s per combo level instead of +1s
        val comboBonus = (comboMultiplier - 1) * 2
        return baseGain + comboBonus
    }

    fun activateDoubleDown(state: MemoryGameState): MemoryGameState {
        val unmatchedPairs = state.cards.count { !it.isMatched } / 2
        // Restriction: Must have at least 3 pairs remaining to activate
        return if (
            state.comboMultiplier >= state.config.heatModeThreshold &&
            !state.isDoubleDownActive &&
            unmatchedPairs >= 3
        ) {
            state.copy(isDoubleDownActive = true)
        } else {
            state
        }
    }

    private const val TIME_ATTACK_BONUS_MULTIPLIER = 10
    private const val DIFF_LEVEL_6 = 6
    private const val DIFF_LEVEL_8 = 8
    private const val DIFF_LEVEL_10 = 10
    private const val DIFF_LEVEL_12 = 12
    private const val INITIAL_TIME_6 = 25L
    private const val INITIAL_TIME_8 = 35L
    private const val INITIAL_TIME_10 = 45L
    private const val INITIAL_TIME_12 = 55L
    private const val TIME_PER_PAIR_FALLBACK = 4
    private const val BASE_TIME_GAIN = 3
    const val TIME_PENALTY_MISMATCH = 2L
}
