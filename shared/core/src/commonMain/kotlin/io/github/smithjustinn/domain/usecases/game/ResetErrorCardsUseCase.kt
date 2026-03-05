package io.github.smithjustinn.domain.usecases.game

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.services.MatchEvaluator

/**
 * Use case to reset cards that were marked as errors (mismatched).
 */
@Inject
open class ResetErrorCardsUseCase {
    open operator fun invoke(state: MemoryGameState): MemoryGameState = MatchEvaluator.resetErrorCards(state)
}
