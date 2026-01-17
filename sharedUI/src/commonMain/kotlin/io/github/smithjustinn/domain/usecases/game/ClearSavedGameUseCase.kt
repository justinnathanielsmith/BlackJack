package io.github.smithjustinn.domain.usecases.game

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Usecase to clear the saved game state.
 */
@Inject
class ClearSavedGameUseCase(
    private val gameStateRepository: GameStateRepository,
    private val logger: Logger
) {
    suspend operator fun invoke() {
        try {
            gameStateRepository.clearSavedGameState()
        } catch (e: Exception) {
            logger.e(e) { "Failed to clear saved game state via use case" }
        }
    }
}
