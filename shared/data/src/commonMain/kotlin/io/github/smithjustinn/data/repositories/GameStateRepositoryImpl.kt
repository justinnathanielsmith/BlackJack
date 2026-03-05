package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStateEntity
import io.github.smithjustinn.di.AppScope
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.domain.repositories.GameStateRepository
import kotlin.coroutines.cancellation.CancellationException

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GameStateRepositoryImpl(
    private val dao: GameStateDao,
    private val logger: Logger,
) : GameStateRepository {
    @Suppress("TooGenericExceptionCaught")
    override suspend fun saveGameState(
        gameState: MemoryGameState,
        elapsedTimeSeconds: Long,
    ) {
        try {
            dao.saveGameState(GameStateEntity(gameState = gameState, elapsedTimeSeconds = elapsedTimeSeconds))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Failed to save game state" }
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun getSavedGameState(): SavedGame? {
        val entity = dao.getSavedGameState() ?: return null
        return try {
            SavedGame(entity.gameState, entity.elapsedTimeSeconds)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Failed to retrieve saved game state: Database error" }
            null
        }
    }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun clearSavedGameState() {
        try {
            dao.clearSavedGameState()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Failed to clear saved game state" }
        }
    }
}
