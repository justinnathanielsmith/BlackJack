package io.github.smithjustinn.data.repository

import kotlinx.coroutines.flow.Flow

interface DailyChallengeRepository {
    fun isChallengeCompleted(date: Long): Flow<Boolean>

    suspend fun saveChallengeResult(
        date: Long,
        score: Int,
        timeSeconds: Long,
        moves: Int,
    )
}
