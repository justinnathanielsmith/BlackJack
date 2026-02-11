package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Use case to initialize a new memory game state.
 */
open class StartNewGameUseCase {
    open operator fun invoke(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
        difficulty: DifficultyType = DifficultyType.CASUAL,
        seed: Long? = null,
    ): MemoryGameState {
        val (finalPairCount, finalSeed) =
            if (mode == GameMode.DAILY_CHALLENGE) {
                // Enforce Daily Challenge invariants: Date-based seed, standard size
                val dailySeed = Clock.System.now().toEpochMilliseconds() / (24 * 60 * 60 * 1000)
                8 to dailySeed
            } else {
                pairCount to (seed ?: Random.nextLong())
            }

        val random = Random(finalSeed)
        val baseState = MemoryGameLogic.createInitialState(finalPairCount, config, mode, difficulty, random)

        val activeMutators =
            if (mode == GameMode.DAILY_CHALLENGE) {
                val mutators = mutableSetOf<DailyChallengeMutator>()
                // Deterministically select mutators based on the seed
                // 50% chance for BLACKOUT
                if (random.nextFloat() < BLACKOUT_CHANCE) {
                    mutators.add(DailyChallengeMutator.BLACKOUT)
                }
                // 40% chance for MIRAGE
                if (random.nextFloat() < MIRAGE_CHANCE) {
                    mutators.add(DailyChallengeMutator.MIRAGE)
                }
                // Ensure at least one mutator is always active for Daily Challenge
                if (mutators.isEmpty()) {
                    mutators.add(DailyChallengeMutator.BLACKOUT)
                }
                mutators
            } else {
                emptySet()
            }

        return baseState.copy(seed = finalSeed, activeMutators = activeMutators)
    }

    companion object {
        private const val BLACKOUT_CHANCE = 0.50f
        private const val MIRAGE_CHANCE = 0.40f
    }
}
