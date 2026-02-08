package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.GameStatsEntity
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

class GameStatsRepositoryImpl(
    private val dao: GameStatsDao,
    private val logger: Logger,
) : GameStatsRepository {
    override fun getStatsForDifficulty(pairCount: Int): Flow<GameStats?> =
        dao
            .getStatsForDifficulty(pairCount)
            .map { it?.toDomain() }
            .catch { e ->
                if (e is CancellationException) throw e
                logger.e(e) { "Error fetching stats for difficulty: $pairCount" }
                emit(null)
            }

    override fun getAllStats(): Flow<List<GameStats>> =
        dao
            .getAllStats()
            .map { entities -> entities.map { it.toDomain() } }
            .catch { e ->
                if (e is CancellationException) throw e
                logger.e(e) { "Error fetching all stats" }
                emit(emptyList())
            }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun updateStats(stats: GameStats) {
        try {
            dao.insertStats(stats.toEntity())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Error updating stats: $stats" }
        }
    }

    private fun GameStatsEntity.toDomain(): GameStats =
        GameStats(
            pairCount = pairCount,
            bestScore = bestScore,
            bestTimeSeconds = bestTimeSeconds,
            gamesPlayed = gamesPlayed,
        )

    private fun GameStats.toEntity(): GameStatsEntity =
        GameStatsEntity(
            pairCount = pairCount,
            bestScore = bestScore,
            bestTimeSeconds = bestTimeSeconds,
            gamesPlayed = gamesPlayed,
        )
}
