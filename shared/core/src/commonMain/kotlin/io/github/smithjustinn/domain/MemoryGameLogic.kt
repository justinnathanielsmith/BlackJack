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
                if (activeCards.size == 1) {
                    newState to GameDomainEvent.CardFlipped
                } else {
                    checkForMatch(newState)
                }
            }
        }
    }

    private fun checkForMatch(state: MemoryGameState): Pair<MemoryGameState, GameDomainEvent?> {
        val activeCards = state.cards.filter { it.isFaceUp && !it.isMatched }

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
        val pointsEarned = config.baseMatchPoints + (state.comboMultiplier - 1) * config.comboBonusPoints

        val isWon = newCards.all { it.isMatched }
        val matchesFound = newCards.count { it.isMatched } / 2
        val moves = state.moves + 1

        val comment = generateMatchComment(moves, matchesFound, state.pairCount, state.comboMultiplier)

        val newState =
            state.copy(
                cards = newCards,
                isGameWon = isWon,
                isGameOver = isWon,
                moves = moves,
                score = state.score + pointsEarned,
                comboMultiplier = state.comboMultiplier + 1,
                matchComment = comment,
                lastMatchedIds = persistentListOf(first.id, second.id),
            )

        return newState to if (isWon) GameDomainEvent.GameWon else GameDomainEvent.MatchSuccess
    }

    private fun handleMatchFailure(
        state: MemoryGameState,
        first: CardState,
        second: CardState,
    ): Pair<MemoryGameState, GameDomainEvent?> {
        val newState =
            state.copy(
                moves = state.moves + 1,
                comboMultiplier = 1,
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
                    matchPoints = state.score,
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
    ): MatchComment {
        if (matchesFound == totalPairs) return MatchComment(Res.string.comment_perfect)

        return when {
            combo > INCREDIBLE_COMBO_THRESHOLD -> {
                MatchComment(Res.string.comment_the_nuts, persistentListOf(combo))
            }

            combo > NICE_COMBO_THRESHOLD -> {
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

            // Toddler
            DIFF_LEVEL_8 -> INITIAL_TIME_8

            // Casual
            DIFF_LEVEL_10 -> INITIAL_TIME_10

            // Master
            DIFF_LEVEL_12 -> INITIAL_TIME_12

            // Shark
            DIFF_LEVEL_14 -> INITIAL_TIME_14

            // Grandmaster
            DIFF_LEVEL_16 -> INITIAL_TIME_16

            // Elephant
            else -> (pairCount * TIME_PER_PAIR_FALLBACK).toLong()
        }

    fun calculateTimeGain(comboMultiplier: Int): Int {
        val baseGain = BASE_TIME_GAIN
        val comboBonus = (comboMultiplier - 1) * COMBO_TIME_GAIN_FACTOR
        return baseGain + comboBonus
    }

    private const val TIME_ATTACK_BONUS_MULTIPLIER = 10
    private const val INCREDIBLE_COMBO_THRESHOLD = 3
    private const val NICE_COMBO_THRESHOLD = 1
    private const val DIFF_LEVEL_6 = 6
    private const val DIFF_LEVEL_8 = 8
    private const val DIFF_LEVEL_10 = 10
    private const val DIFF_LEVEL_12 = 12
    private const val DIFF_LEVEL_14 = 14
    private const val DIFF_LEVEL_16 = 16
    private const val INITIAL_TIME_6 = 25L
    private const val INITIAL_TIME_8 = 35L
    private const val INITIAL_TIME_10 = 45L
    private const val INITIAL_TIME_12 = 55L
    private const val INITIAL_TIME_14 = 65L
    private const val INITIAL_TIME_16 = 75L
    private const val TIME_PER_PAIR_FALLBACK = 4
    private const val BASE_TIME_GAIN = 3
    private const val COMBO_TIME_GAIN_FACTOR = 1

    const val TIME_PENALTY_MISMATCH = 2L
}
