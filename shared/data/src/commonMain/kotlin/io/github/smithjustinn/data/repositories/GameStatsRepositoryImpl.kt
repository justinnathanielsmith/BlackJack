package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.extensions.mapList
import io.github.smithjustinn.data.extensions.mapNullable
import io.github.smithjustinn.data.local.GameStatsDao
import io.github.smithjustinn.data.local.GameStatsEntity
import io.github.smithjustinn.di.AppScope
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlin.coroutines.cancellation.CancellationException

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class GameStatsRepositoryImpl(
    private val dao: GameStatsDao,
    private val logger: Logger,
) : GameStatsRepository {
    override fun getStatsForDifficulty(pairCount: Int): Flow<GameStats?> =
        dao
            .getStatsForDifficulty(pairCount)
            .mapNullable { it.toDomain() }
            .catch { e ->
                if (e is CancellationException) throw e
                logger.e(e) { "Error fetching stats for difficulty: $pairCount" }
                emit(null)
            }

    override fun getAllStats(): Flow<List<GameStats>> =
        dao
            .getAllStats()
            .mapList { it.toDomain() }
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

    private fun GameStatsEntity.toDomain(): GameStats = GameStats(pairCount, bestScore, bestTimeSeconds, gamesPlayed)

    private fun GameStats.toEntity(): GameStatsEntity =
        GameStatsEntity(pairCount, bestScore, bestTimeSeconds, gamesPlayed)
}
