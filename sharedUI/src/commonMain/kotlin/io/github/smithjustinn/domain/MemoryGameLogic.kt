package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import memory_match.sharedui.generated.resources.*

/**
 * Pure logic for the Memory Match game.
 */
object MemoryGameLogic {

    fun createInitialState(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.STANDARD,
    ): MemoryGameState {
        val allPossibleCards = Suit.entries.flatMap { suit ->
            Rank.entries.map { rank -> suit to rank }
        }.shuffled()

        val selectedPairs = allPossibleCards.take(pairCount)

        val gameCards = selectedPairs.flatMap { (suit, rank) ->
            listOf(
                CardState(id = 0, suit = suit, rank = rank),
                CardState(id = 0, suit = suit, rank = rank),
            )
        }.shuffled().mapIndexed { index, card ->
            card.copy(id = index)
        }.toImmutableList()

        return MemoryGameState(
            cards = gameCards,
            pairCount = pairCount,
            config = config,
            mode = mode,
        )
    }

    fun flipCard(state: MemoryGameState, cardId: Int): Pair<MemoryGameState, GameDomainEvent?> {
        if (state.isGameOver) return state to null

        val cardToFlip = state.cards.find { it.id == cardId } ?: return state to null

        if (cardToFlip.isFaceUp || cardToFlip.isMatched) return state to null

        val faceUpCards = state.cards.filter { it.isFaceUp && !it.isMatched }
        if (faceUpCards.size >= 2) return state to null

        val newState = state.copy(
            cards = state.cards.map { card ->
                if (card.id == cardId) card.copy(isFaceUp = true) else card
            }.toImmutableList(),
            // Clear last matched IDs when starting a new turn
            lastMatchedIds = if (faceUpCards.isEmpty()) persistentListOf() else state.lastMatchedIds,
        )

        val activeCards = newState.cards.filter { it.isFaceUp && !it.isMatched }
        if (activeCards.size == 1) {
            return newState to GameDomainEvent.CardFlipped
        }

        return checkForMatch(newState)
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

    private fun handleMatchSuccess(state: MemoryGameState, first: CardState, second: CardState): Pair<MemoryGameState, GameDomainEvent?> {
        val newCards = state.cards.map { card ->
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

        val newState = state.copy(
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

    private fun handleMatchFailure(state: MemoryGameState, first: CardState, second: CardState): Pair<MemoryGameState, GameDomainEvent?> {
        val newState = state.copy(
            moves = state.moves + 1,
            comboMultiplier = 1,
            cards = state.cards.map { card ->
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

    fun resetErrorCards(state: MemoryGameState): MemoryGameState = state.copy(
        cards = state.cards.map { card ->
            if (card.isError) card.copy(isFaceUp = false, isError = false) else card
        }.toImmutableList(),
    )

    /**
     * Calculates and applies bonuses to the final score when the game is won.
     * The move efficiency is now the dominant factor.
     */
    fun applyFinalBonuses(state: MemoryGameState, elapsedTimeSeconds: Long): MemoryGameState {
        if (!state.isGameWon) return state

        val config = state.config

        // Time Bonus: Small impact
        val timeBonus = if (state.mode == GameMode.TIME_ATTACK) {
            // In Time Attack, remaining time is the bonus
            (elapsedTimeSeconds * 10).toInt() // Example: 10 points per remaining second
        } else {
            (state.pairCount * config.timeBonusPerPair - (elapsedTimeSeconds * config.timePenaltyPerSecond))
                .coerceAtLeast(0).toInt()
        }

        // Move Efficiency Bonus: Dominant factor
        val moveEfficiency = state.pairCount.toDouble() / state.moves.toDouble()
        val moveBonus = (moveEfficiency * config.moveBonusMultiplier).toInt()

        val totalScore = state.score + timeBonus + moveBonus

        return state.copy(
            score = totalScore,
            scoreBreakdown = ScoreBreakdown(
                matchPoints = state.score,
                timeBonus = timeBonus,
                moveBonus = moveBonus,
                totalScore = totalScore,
            ),
        )
    }

    private fun generateMatchComment(moves: Int, matchesFound: Int, totalPairs: Int, combo: Int): MatchComment {
        if (matchesFound == totalPairs) return MatchComment(Res.string.comment_perfect)

        return when {
            combo > 3 -> MatchComment(Res.string.comment_incredible, persistentListOf(combo))
            combo > 1 -> MatchComment(Res.string.comment_nice, persistentListOf(combo))
            matchesFound == 1 -> MatchComment(Res.string.comment_first_match)
            matchesFound == totalPairs / 2 -> MatchComment(Res.string.comment_halfway)
            moves <= matchesFound * 2 -> MatchComment(Res.string.comment_photographic)
            matchesFound == totalPairs - 1 -> MatchComment(Res.string.comment_one_more)
            else -> {
                val randomRes = listOf(
                    Res.string.comment_great_find,
                    Res.string.comment_you_got_it,
                    Res.string.comment_boom,
                    Res.string.comment_eagle_eyes,
                    Res.string.comment_sharp,
                    Res.string.comment_on_a_roll,
                    Res.string.comment_keep_it_up,
                ).random()
                MatchComment(randomRes)
            }
        }
    }

    // --- Time Attack Logic ---

    fun calculateInitialTime(pairCount: Int): Long = when (pairCount) {
        6 -> 25L // Toddler
        8 -> 35L // Casual
        10 -> 45L // Master
        12 -> 55L // Shark
        14 -> 65L // Grandmaster
        16 -> 75L // Elephant
        else -> (pairCount * 4).toLong()
    }

    fun calculateTimeGain(comboMultiplier: Int): Int {
        val baseGain = 3
        val comboBonus = (comboMultiplier - 1) * 1
        return baseGain + comboBonus
    }

    const val TIME_PENALTY_MISMATCH = 2L
}
