package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.GameStats
import kotlinx.coroutines.flow.Flow

interface GameStatsRepository {
    fun getStatsForDifficulty(pairCount: Int): Flow<GameStats?>

    suspend fun updateStats(stats: GameStats)
}
