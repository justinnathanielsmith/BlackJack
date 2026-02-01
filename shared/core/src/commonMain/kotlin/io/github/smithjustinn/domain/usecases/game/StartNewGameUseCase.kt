package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.random.Random

/**
 * Use case to initialize a new memory game state.
 */
open class StartNewGameUseCase {
    open operator fun invoke(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.TIME_ATTACK,
        seed: Long? = null,
    ): MemoryGameState {
        val finalSeed = seed ?: Random.nextLong()
        val random = Random(finalSeed)
        val baseState = MemoryGameLogic.createInitialState(pairCount, config, mode, random)

        val activeMutators = if (mode == GameMode.DAILY_CHALLENGE) {
            val mutators = mutableSetOf<DailyChallengeMutator>()
            // Deterministically select mutators based on the seed
            // 50% chance for BLACKOUT
            if (random.nextBoolean()) {
                mutators.add(DailyChallengeMutator.BLACKOUT)
            }
            // 40% chance for MIRAGE
            if (random.nextFloat() < 0.40f) {
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
}
