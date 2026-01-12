package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository

/**
 * Usecase to save the current game state.
 */
@Inject
class SaveGameStateUseCase(
    private val gameStateRepository: GameStateRepository
) {
    suspend operator fun invoke(state: MemoryGameState, elapsedTimeSeconds: Long) {
        gameStateRepository.saveGameState(state, elapsedTimeSeconds)
    }
}
