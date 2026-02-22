package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import io.github.smithjustinn.domain.services.GameFactory
import io.github.smithjustinn.utils.TimeConstants
import io.github.smithjustinn.utils.secureRandomLong
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
        val params = resolveGameParameters(mode, pairCount, config, difficulty, seed)

        val random = Random(params.seed)
        val baseState =
            GameFactory.createInitialState(
                pairCount = params.pairCount,
                config = params.config,
                mode = mode,
                difficulty = params.difficulty,
                isHeatShieldAvailable = isHeatShieldEnabled,
                random = random,
            )

        val activeMutators = determineActiveMutators(mode, random)

        return baseState.copy(seed = params.seed, activeMutators = activeMutators)
    }

    private fun resolveGameParameters(
        mode: GameMode,
        pairCount: Int,
        config: ScoringConfig,
        difficulty: DifficultyType,
        seed: Long?,
    ): GameParameters =
        if (mode == GameMode.DAILY_CHALLENGE) {
            GameParameters(
                pairCount = DAILY_CHALLENGE_PAIR_COUNT,
                seed = Clock.System.now().toEpochMilliseconds() / TimeConstants.MILLIS_IN_DAY,
                config = ScoringConfig(),
                difficulty = DifficultyType.CASUAL,
            )
        } else {
            require(pairCount in 1..MAX_PAIR_COUNT) { "Pair count must be between 1 and $MAX_PAIR_COUNT" }
            GameParameters(
                pairCount = pairCount,
                seed = seed ?: secureRandomLong(),
                config = config,
                difficulty = difficulty,
            )
        }

    private fun determineActiveMutators(
        mode: GameMode,
        random: Random,
    ): Set<DailyChallengeMutator> {
        if (mode != GameMode.DAILY_CHALLENGE) return emptySet()

        val mutators = mutableSetOf<DailyChallengeMutator>()
        if (random.nextFloat() < BLACKOUT_CHANCE) {
            mutators.add(DailyChallengeMutator.BLACKOUT)
        }
        if (random.nextFloat() < MIRAGE_CHANCE) {
            mutators.add(DailyChallengeMutator.MIRAGE)
        }
        if (mutators.isEmpty()) {
            mutators.add(DailyChallengeMutator.BLACKOUT)
        }
        return mutators
    }

    private data class GameParameters(
        val pairCount: Int,
        val seed: Long,
        val config: ScoringConfig,
        val difficulty: DifficultyType,
    )

    companion object {
        const val MAX_PAIR_COUNT = 26
        private const val DAILY_CHALLENGE_PAIR_COUNT = 8

        private const val BLACKOUT_CHANCE = 0.50f
        private const val MIRAGE_CHANCE = 0.40f
    }
}
