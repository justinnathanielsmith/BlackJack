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
        isHeatShieldEnabled: Boolean = false,
        seed: Long? = null,
    ): MemoryGameState {
        val (finalPairCount, finalSeed) =
            if (mode == GameMode.DAILY_CHALLENGE) {
                // Enforce Daily Challenge invariants: Date-based seed, standard size
                val dailySeed = Clock.System.now().toEpochMilliseconds() / MILLIS_IN_DAY
                DAILY_CHALLENGE_PAIR_COUNT to dailySeed
            } else {
                require(pairCount in 1..MAX_PAIR_COUNT) { "Pair count must be between 1 and $MAX_PAIR_COUNT" }
                pairCount to (seed ?: Random.nextLong())
            }

        val random = Random(finalSeed)
        val baseState = MemoryGameLogic.createInitialState(
            pairCount = finalPairCount,
            config = config,
            mode = mode,
            difficulty = difficulty,
            isHeatShieldEnabled = isHeatShieldEnabled,
            random = random
        )

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
        const val MAX_PAIR_COUNT = 26
        private const val DAILY_CHALLENGE_PAIR_COUNT = 8
        private const val MILLIS_IN_SECOND = 1000L
        private const val SECONDS_IN_MINUTE = 60L
        private const val MINUTES_IN_HOUR = 60L
        private const val HOURS_IN_DAY = 24L
        private const val MILLIS_IN_DAY = HOURS_IN_DAY * MINUTES_IN_HOUR * SECONDS_IN_MINUTE * MILLIS_IN_SECOND

        private const val BLACKOUT_CHANCE = 0.50f
        private const val MIRAGE_CHANCE = 0.40f
    }
}
