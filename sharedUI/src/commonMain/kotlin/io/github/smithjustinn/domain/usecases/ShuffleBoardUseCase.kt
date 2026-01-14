package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Use case to shuffle the remaining unmatched cards on the board.
 */
@Inject
class ShuffleBoardUseCase {
    operator fun invoke(state: MemoryGameState): MemoryGameState {
        return MemoryGameLogic.shuffleRemainingCards(state)
    }
}
