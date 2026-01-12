package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Usecase to clear the saved game state.
 */
@Inject
class ClearSavedGameUseCase(
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke() {
        gameStateRepository.clearSavedGameState()
    }
}
