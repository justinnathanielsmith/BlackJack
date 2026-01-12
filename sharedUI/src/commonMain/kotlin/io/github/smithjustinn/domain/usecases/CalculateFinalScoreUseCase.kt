package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Use case to calculate and apply final bonuses when a game is won.
 */
@Inject
class CalculateFinalScoreUseCase {
    operator fun invoke(state: MemoryGameState, elapsedTimeSeconds: Long): MemoryGameState {
        return MemoryGameLogic.applyFinalBonuses(state, elapsedTimeSeconds)
    }
}
