package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Use case to retrieve the saved game state.
 */
@Inject
class GetSavedGameUseCase(
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke(): Pair<MemoryGameState, Long>? {
        return gameStateRepository.getSavedGameState()
    }
}
