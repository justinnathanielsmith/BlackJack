package io.github.smithjustinn.domain.usecases.game

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Use case to calculate and apply final bonuses when a game is won.
 */
@Inject
open class CalculateFinalScoreUseCase {
    open operator fun invoke(state: MemoryGameState, elapsedTimeSeconds: Long): MemoryGameState {
        return MemoryGameLogic.applyFinalBonuses(state, elapsedTimeSeconds)
    }
}
