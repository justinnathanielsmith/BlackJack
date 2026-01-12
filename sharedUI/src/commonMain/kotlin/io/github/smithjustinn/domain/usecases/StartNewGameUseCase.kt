package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig

/**
 * Use case to initialize a new memory game state.
 */
@Inject
class StartNewGameUseCase {
    operator fun invoke(pairCount: Int, config: ScoringConfig = ScoringConfig()): MemoryGameState {
        return MemoryGameLogic.createInitialState(pairCount, config)
    }
}
