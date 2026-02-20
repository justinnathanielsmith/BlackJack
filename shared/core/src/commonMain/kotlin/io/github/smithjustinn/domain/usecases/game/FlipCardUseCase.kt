package io.github.smithjustinn.domain.usecases.game

import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.services.MatchEvaluator

/**
 * Use case to handle the logic of flipping a card and processing matches.
 */
open class FlipCardUseCase {
    open operator fun invoke(
        state: MemoryGameState,
        cardId: Int,
    ): Pair<MemoryGameState, GameDomainEvent?> = MatchEvaluator.flipCard(state, cardId)
}
