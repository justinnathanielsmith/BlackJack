package io.github.smithjustinn.domain.usecases.stats

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.GameMode
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
open class SaveGameResultUseCase(
    private val gameStatsRepository: GameStatsRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val logger: Logger
) {
    suspend open operator fun invoke(pairCount: Int, score: Int, timeSeconds: Long, moves: Int, gameMode: GameMode) {
        try {
            // Stats are currently per difficulty, we might want to separate them by mode too in the future
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
                    timestamp = Clock.System.now(),
                    gameMode = gameMode
                )
            )
        } catch (e: Exception) {
            logger.e(e) { "Failed to save game result via use case" }
        }
    }
}
