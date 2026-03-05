package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.data.local.LeaderboardDao
import io.github.smithjustinn.data.local.LeaderboardEntity
import io.github.smithjustinn.di.AppScope
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlin.coroutines.cancellation.CancellationException

@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class LeaderboardRepositoryImpl(
    private val dao: LeaderboardDao,
    private val logger: Logger,
) : LeaderboardRepository {
    override fun getTopEntries(
        pairCount: Int,
        gameMode: GameMode,
    ): Flow<List<LeaderboardEntry>> =
        dao
            .getTopEntries(pairCount, gameMode)
            .mapToDomain()
            .catch { e ->
                if (e is CancellationException) throw e
                logger.e(e) { "Error fetching leaderboard for difficulty: $pairCount, mode: $gameMode" }
                emit(emptyList())
            }

    override fun getAllTopEntries(gameMode: GameMode): Flow<List<LeaderboardEntry>> =
        dao
            .getAllTopEntries(gameMode)
            .mapToDomain()
            .catch { e ->
                if (e is CancellationException) throw e
                logger.e(e) { "Error fetching all leaderboards for mode: $gameMode" }
                emit(emptyList())
            }

    @Suppress("TooGenericExceptionCaught")
    override suspend fun addEntry(entry: LeaderboardEntry) {
        try {
            dao.insertEntry(entry.toEntity())
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            logger.e(e) { "Error adding leaderboard entry: $entry" }
        }
    }

    private fun LeaderboardEntity.toDomain(): LeaderboardEntry =
        LeaderboardEntry(
            id = id,
            pairCount = pairCount,
            score = score,
            timeSeconds = timeSeconds,
            moves = moves,
            timestamp = timestamp,
            gameMode = gameMode,
        )

    private fun LeaderboardEntry.toEntity(): LeaderboardEntity =
        LeaderboardEntity(
            id = id,
            pairCount = pairCount,
            score = score,
            timeSeconds = timeSeconds,
            moves = moves,
            timestamp = timestamp,
            gameMode = gameMode,
        )

    private fun Flow<List<LeaderboardEntity>>.mapToDomain(): Flow<List<LeaderboardEntry>> =
        map { entities -> entities.map { it.toDomain() } }
}
