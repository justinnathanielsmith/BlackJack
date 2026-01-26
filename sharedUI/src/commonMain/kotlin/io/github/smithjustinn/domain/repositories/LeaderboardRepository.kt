package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry
import kotlinx.coroutines.flow.Flow

interface LeaderboardRepository {
    fun getTopEntries(
        pairCount: Int,
        gameMode: GameMode,
    ): Flow<List<LeaderboardEntry>>

    suspend fun addEntry(entry: LeaderboardEntry)
}
