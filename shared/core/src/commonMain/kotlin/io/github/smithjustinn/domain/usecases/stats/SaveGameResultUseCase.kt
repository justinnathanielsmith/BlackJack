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
            val currentStats = gameStatsRepository.getStatsForDifficulty(pairCount).firstOrNull()
            val updatedStats = calculateUpdatedStats(currentStats, pairCount, score, timeSeconds)

            gameStatsRepository.updateStats(updatedStats)

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

    private fun calculateUpdatedStats(
        current: GameStats?,
        pairCount: Int,
        score: Int,
        time: Long,
    ): GameStats {
        val newBestScore =
            if (current == null || score > current.bestScore) {
                score
            } else {
                current.bestScore
            }

        val newBestTime =
            if (current == null || current.bestTimeSeconds == 0L || time < current.bestTimeSeconds) {
                time
            } else {
                current.bestTimeSeconds
            }

        return GameStats(
            pairCount = pairCount,
            bestScore = newBestScore,
            bestTimeSeconds = newBestTime,
            gamesPlayed = (current?.gamesPlayed ?: 0) + 1,
        )
    }
}
