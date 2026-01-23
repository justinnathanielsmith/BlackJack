package io.github.smithjustinn.domain.usecases.game

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.MemoryGameState

/**
 * Use case to handle the logic of flipping a card and processing matches.
 */
@Inject
open class FlipCardUseCase {
    open operator fun invoke(state: MemoryGameState, cardId: Int): Pair<MemoryGameState, GameDomainEvent?> {
        return MemoryGameLogic.flipCard(state, cardId)
    }
}
