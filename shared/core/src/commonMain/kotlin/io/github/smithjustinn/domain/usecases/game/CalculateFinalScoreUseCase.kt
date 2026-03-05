package io.github.smithjustinn.domain.usecases.game

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.services.ScoreKeeper

/**
 * Use case to calculate and apply final bonuses when a game is won.
 */
@Inject
open class CalculateFinalScoreUseCase {
    open operator fun invoke(
        state: MemoryGameState,
        elapsedTimeSeconds: Long,
    ): MemoryGameState = ScoreKeeper.applyFinalBonuses(state, elapsedTimeSeconds)
}
