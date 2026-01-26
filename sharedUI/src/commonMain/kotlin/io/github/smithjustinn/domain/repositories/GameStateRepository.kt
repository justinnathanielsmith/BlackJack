package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.MemoryGameState

interface GameStateRepository {
    suspend fun saveGameState(
        gameState: MemoryGameState,
        elapsedTimeSeconds: Long,
    )

    suspend fun getSavedGameState(): Pair<MemoryGameState, Long>?

    suspend fun clearSavedGameState()
}
