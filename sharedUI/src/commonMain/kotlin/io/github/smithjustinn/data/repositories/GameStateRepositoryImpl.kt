package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStateEntity
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository
import kotlinx.serialization.json.Json

@Inject
internal class GameStateRepositoryImpl(
    private val dao: GameStateDao,
    private val json: Json,
    private val logger: Logger,
) : GameStateRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveGameState(gameState: MemoryGameState, elapsedTimeSeconds: Long) {
        try {
            val jsonString = json.encodeToString(gameState)
            dao.saveGameState(GameStateEntity(gameStateJson = jsonString, elapsedTimeSeconds = elapsedTimeSeconds))
        } catch (e: Exception) {
            logger.e(e) { "Failed to save game state" }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getSavedGameState(): Pair<MemoryGameState, Long>? {
        val entity = dao.getSavedGameState() ?: return null
        return try {
            val gameState = json.decodeFromString<MemoryGameState>(entity.gameStateJson)
            gameState to entity.elapsedTimeSeconds
        } catch (e: kotlinx.serialization.SerializationException) {
            logger.e(e) { "Failed to decode saved game state: JSON corruption" }
            null
        } catch (e: Exception) {
            logger.e(e) { "Failed to retrieve saved game state: Database error" }
            null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun clearSavedGameState() {
        try {
            dao.clearSavedGameState()
        } catch (e: Exception) {
            logger.e(e) { "Failed to clear saved game state" }
        }
    }
}
