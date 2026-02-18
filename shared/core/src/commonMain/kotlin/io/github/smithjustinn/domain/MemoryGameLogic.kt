package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.domain.services.MatchEvaluator
import io.github.smithjustinn.domain.services.MutatorEngine
import io.github.smithjustinn.domain.services.ScoreKeeper
import kotlin.random.Random

/**
 * Pure logic for the Memory Match game.
 * This object now delegates to specialized domain services.
 *
 * @deprecated Use the individual services instead:
 * - [GameFactory] for state creation
 * - [MatchEvaluator] for card flipping and matching
 * - [ScoreKeeper] for scoring
 * - [MutatorEngine] for mutators
 */
@Deprecated("Use specialized services: GameFactory, MatchEvaluator, ScoreKeeper, MutatorEngine")
object MemoryGameLogic {
    const val MIN_PAIRS_FOR_DOUBLE_DOWN = 3

    fun createInitialState(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
        difficulty: DifficultyType = DifficultyType.CASUAL,
        isHeatShieldAvailable: Boolean = false,
        random: Random = Random,
    ): MemoryGameState =
        GameFactory.createInitialState(
            pairCount = pairCount,
            config = config,
            mode = mode,
            difficulty = difficulty,
            isHeatShieldAvailable = isHeatShieldAvailable,
            random = random,
        )

    fun flipCard(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> = MatchEvaluator.flipCard(state, cardId)

    fun resetErrorCards(state: MemoryGameState): MemoryGameState = MatchEvaluator.resetErrorCards(state)

    fun resetUnmatchedCards(state: MemoryGameState): MemoryGameState = MatchEvaluator.resetUnmatchedCards(state)

    fun applyFinalBonuses(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = ScoreKeeper.applyFinalBonuses(state, elapsedTimeSeconds)

    fun activateDoubleDown(state: MemoryGameState): MemoryGameState = MatchEvaluator.activateDoubleDown(state)

    fun applyMutators(
        state: MemoryGameState,
        random: Random = Random,
    ): MemoryGameState = MutatorEngine.applyMutators(state, random)
}

/**
 * Secondary actions and mutators for the Memory Match game.
 * Restored for backward compatibility.
 *
 * @deprecated Use [MatchEvaluator] and [MutatorEngine] directly
 */
@Deprecated("Use [MatchEvaluator] and [MutatorEngine] directly")
object MemoryGameActions {
    fun resetErrorCards(state: MemoryGameState): MemoryGameState = MatchEvaluator.resetErrorCards(state)

    fun resetUnmatchedCards(state: MemoryGameState): MemoryGameState = MatchEvaluator.resetUnmatchedCards(state)

    fun activateDoubleDown(state: MemoryGameState): MemoryGameState = MatchEvaluator.activateDoubleDown(state)

    fun applyMutators(
        state: MemoryGameState,
        random: Random = Random,
    ): MemoryGameState = MutatorEngine.applyMutators(state, random)
}
