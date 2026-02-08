package io.github.smithjustinn.domain.usecases.stats

import co.touchlab.kermit.Logger
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
open class SaveGameResultUseCase(
    private val gameStatsRepository: GameStatsRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val logger: Logger,
) {
    open suspend operator fun invoke(
        pairCount: Int,
        score: Int,
        timeSeconds: Long,
        moves: Int,
        gameMode: GameMode,
    ): Result<Unit> =
        runCatching {
            // Stats are currently per difficulty, we might want to separate them by mode too in the future
            val currentStats = gameStatsRepository.getStatsForDifficulty(pairCount).firstOrNull()

            val newBestScore =
                if (currentStats == null ||
                    score > currentStats.bestScore
                ) {
                    score
                } else {
                    currentStats.bestScore
                }
            val newBestTime =
                if (currentStats == null ||
                    currentStats.bestTimeSeconds == 0L ||
                    timeSeconds < currentStats.bestTimeSeconds
                ) {
                    timeSeconds
                } else {
                    currentStats.bestTimeSeconds
                }

            val newGamesPlayed = (currentStats?.gamesPlayed ?: 0) + 1

            gameStatsRepository.updateStats(
                GameStats(
                    pairCount = pairCount,
                    bestScore = newBestScore,
                    bestTimeSeconds = newBestTime,
                    gamesPlayed = newGamesPlayed,
                ),
            )

            leaderboardRepository.addEntry(
                LeaderboardEntry(
                    pairCount = pairCount,
                    score = score,
                    timeSeconds = timeSeconds,
                    moves = moves,
                    timestamp = Clock.System.now(),
                    gameMode = gameMode,
                ),
            )
        }.onFailure { e ->
            logger.e(e) { "Failed to save game result via use case" }
        }
}
