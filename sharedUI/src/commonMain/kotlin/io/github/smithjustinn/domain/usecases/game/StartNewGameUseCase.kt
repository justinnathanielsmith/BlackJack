package io.github.smithjustinn.domain.usecases.game

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig
import kotlin.random.Random

/**
 * Use case to initialize a new memory game state.
 */
@Inject
open class StartNewGameUseCase {
    open operator fun invoke(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.STANDARD,
        seed: Long? = null,
    ): MemoryGameState {
        val finalSeed = seed ?: Random.nextLong()
        val random = Random(finalSeed)
        return MemoryGameLogic.createInitialState(pairCount, config, mode, random).copy(seed = finalSeed)
    }
}
