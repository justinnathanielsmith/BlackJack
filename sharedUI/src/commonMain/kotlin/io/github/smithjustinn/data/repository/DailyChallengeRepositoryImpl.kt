package io.github.smithjustinn.data.repository

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.data.local.DailyChallengeDao
import io.github.smithjustinn.data.local.DailyChallengeEntity
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@Inject
class DailyChallengeRepositoryImpl(private val dao: DailyChallengeDao, private val dispatchers: CoroutineDispatchers) :
    DailyChallengeRepository {
    override fun isChallengeCompleted(date: Long): Flow<Boolean> = dao.getDailyChallenge(date).map {
        it?.isCompleted ==
            true
    }

    override suspend fun saveChallengeResult(date: Long, score: Int, timeSeconds: Long, moves: Int) {
        withContext(dispatchers.io) {
            val entity = DailyChallengeEntity(
                date = date,
                isCompleted = true,
                score = score,
                timeSeconds = timeSeconds,
                moves = moves,
            )
            dao.insertOrUpdate(entity)
        }
    }
}
