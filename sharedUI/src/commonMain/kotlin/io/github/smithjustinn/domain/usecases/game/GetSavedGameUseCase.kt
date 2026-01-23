package io.github.smithjustinn.domain.usecases.game

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Use case to retrieve the saved game state.
 */
@Inject
open class GetSavedGameUseCase(
    private val gameStateRepository: GameStateRepository,
    private val logger: Logger
) {
    suspend open operator fun invoke(): Pair<MemoryGameState, Long>? {
        return try {
            gameStateRepository.getSavedGameState()
        } catch (e: Exception) {
            logger.e(e) { "Failed to get saved game state via use case" }
            null
        }
    }
}
