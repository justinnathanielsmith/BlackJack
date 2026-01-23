package io.github.smithjustinn.domain.usecases.game

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Usecase to save the current game state.
 */
@Inject
open class SaveGameStateUseCase(
    private val gameStateRepository: GameStateRepository,
    private val logger: Logger
) {
    suspend open operator fun invoke(state: MemoryGameState, elapsedTimeSeconds: Long) {
        try {
            gameStateRepository.saveGameState(state, elapsedTimeSeconds)
        } catch (e: Exception) {
            logger.e(e) { "Failed to save game state via use case" }
        }
    }
}
