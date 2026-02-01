package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStateEntity
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.domain.repositories.GameStateRepository
import kotlinx.serialization.json.Json

class GameStateRepositoryImpl(
    private val dao: GameStateDao,
    private val json: Json,
    private val logger: Logger,
) : GameStateRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveGameState(
        gameState: MemoryGameState,
        elapsedTimeSeconds: Long,
    ) {
        try {
            dao.saveGameState(GameStateEntity(gameState = gameState, elapsedTimeSeconds = elapsedTimeSeconds))
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            logger.e(e) { "Failed to save game state" }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getSavedGameState(): SavedGame? {
        val entity = dao.getSavedGameState() ?: return null
        return try {
            SavedGame(entity.gameState, entity.elapsedTimeSeconds)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            logger.e(e) { "Failed to retrieve saved game state: Database error" }
            null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun clearSavedGameState() {
        try {
            dao.clearSavedGameState()
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            logger.e(e) { "Failed to clear saved game state" }
        }
    }
}
