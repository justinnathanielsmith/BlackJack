package io.github.smithjustinn.domain.usecases

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import kotlinx.coroutines.flow.firstOrNull
import kotlin.time.Clock

/**
 * Use case to save the result of a completed game.
 * Handles updating the best scores and adding to the leaderboard.
 */
@Inject
class SaveGameResultUseCase(
    private val gameStatsRepository: GameStatsRepository,
    private val leaderboardRepository: LeaderboardRepository
) {
    suspend operator fun invoke(pairCount: Int, score: Int, timeSeconds: Long, moves: Int) {
        val currentStats = gameStatsRepository.getStatsForDifficulty(pairCount).firstOrNull()
        
        val newBestScore = if (currentStats == null || score > currentStats.bestScore) score else currentStats.bestScore
        val newBestTime = if (currentStats == null || currentStats.bestTimeSeconds == 0L || timeSeconds < currentStats.bestTimeSeconds) {
            timeSeconds
        } else {
            currentStats.bestTimeSeconds
        }
        
        gameStatsRepository.updateStats(GameStats(pairCount, newBestScore, newBestTime))
        
        leaderboardRepository.addEntry(
            LeaderboardEntry(
                pairCount = pairCount,
                score = score,
                timeSeconds = timeSeconds,
                moves = moves,
                timestamp = Clock.System.now()
            )
        )
    }
}
