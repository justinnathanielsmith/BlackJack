package io.github.smithjustinn.data.repositories

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStateEntity
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository
import kotlinx.serialization.json.Json

@Inject
class GameStateRepositoryImpl(
    private val dao: GameStateDao,
    private val json: Json
) : GameStateRepository {
    override suspend fun saveGameState(gameState: MemoryGameState, elapsedTimeSeconds: Long) {
        val jsonString = json.encodeToString(gameState)
        dao.saveGameState(GameStateEntity(gameStateJson = jsonString, elapsedTimeSeconds = elapsedTimeSeconds))
    }

    override suspend fun getSavedGameState(): Pair<MemoryGameState, Long>? {
        val entity = dao.getSavedGameState() ?: return null
        return try {
            val gameState = json.decodeFromString<MemoryGameState>(entity.gameStateJson)
            gameState to entity.elapsedTimeSeconds
        } catch (_: Exception) {
            null
        }
    }

    override suspend fun clearSavedGameState() {
        dao.clearSavedGameState()
    }
}
