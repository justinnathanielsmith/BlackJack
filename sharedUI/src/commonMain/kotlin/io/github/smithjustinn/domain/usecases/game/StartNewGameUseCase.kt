package io.github.smithjustinn.domain.usecases.game

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoringConfig

/**
 * Use case to initialize a new memory game state.
 */
@Inject
open class StartNewGameUseCase {
    open operator fun invoke(
        pairCount: Int,
        config: ScoringConfig = ScoringConfig(),
        mode: GameMode = GameMode.STANDARD
    ): MemoryGameState {
        return MemoryGameLogic.createInitialState(pairCount, config, mode)
    }
}
