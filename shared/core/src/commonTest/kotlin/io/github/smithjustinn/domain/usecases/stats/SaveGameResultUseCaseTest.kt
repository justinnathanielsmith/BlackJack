package io.github.smithjustinn.domain.usecases.stats

import co.touchlab.kermit.Logger
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SaveGameResultUseCaseTest {
    private val statsRepository = mock<GameStatsRepository>()
    private val leaderboardRepository = mock<LeaderboardRepository>()
    private val logger = Logger.withTag("Test")
    private val useCase = SaveGameResultUseCase(statsRepository, leaderboardRepository, logger)

    @Test
    fun testInvoke_newBestScore() =
        runTest {
            val pairCount = 8
            val score = 100
            val time = 60L
            val moves = 20

            everySuspend { statsRepository.getStatsForDifficulty(pairCount) } returns flowOf(null)
            everySuspend { statsRepository.updateStats(any()) } returns Unit
            everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

            useCase(pairCount, score, time, moves, GameMode.TIME_ATTACK)

            verifySuspend {
                statsRepository.updateStats(GameStats(pairCount, score, time, gamesPlayed = 1))
                leaderboardRepository.addEntry(any())
            }
        }

    @Test
    fun testInvoke_notBestScore() =
        runTest {
            val pairCount = 8
            val score = 50
            val time = 100L
            val moves = 20
            val existingStats = GameStats(pairCount, 100, 50L, gamesPlayed = 0)

            everySuspend { statsRepository.getStatsForDifficulty(pairCount) } returns flowOf(existingStats)
            everySuspend { statsRepository.updateStats(any()) } returns Unit
            everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

            useCase(pairCount, score, time, moves, GameMode.TIME_ATTACK)

            verifySuspend {
                statsRepository.updateStats(GameStats(pairCount, 100, 50L, gamesPlayed = 1))
                leaderboardRepository.addEntry(any())
            }
        }

    @Test
    fun testInvoke_betterScore_worseTime() =
        runTest {
            val pairCount = 8
            val score = 200
            val time = 100L
            val moves = 20
            val existingStats = GameStats(pairCount, 100, 50L, gamesPlayed = 1)

            everySuspend { statsRepository.getStatsForDifficulty(pairCount) } returns flowOf(existingStats)
            everySuspend { statsRepository.updateStats(any()) } returns Unit
            everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

            useCase(pairCount, score, time, moves, GameMode.TIME_ATTACK)

            verifySuspend {
                statsRepository.updateStats(GameStats(pairCount, score, 50L, gamesPlayed = 2))
                leaderboardRepository.addEntry(any())
            }
        }

    @Test
    fun testInvoke_worseScore_betterTime() =
        runTest {
            val pairCount = 8
            val score = 50
            val time = 40L
            val moves = 20
            val existingStats = GameStats(pairCount, 100, 50L, gamesPlayed = 1)

            everySuspend { statsRepository.getStatsForDifficulty(pairCount) } returns flowOf(existingStats)
            everySuspend { statsRepository.updateStats(any()) } returns Unit
            everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

            useCase(pairCount, score, time, moves, GameMode.TIME_ATTACK)

            verifySuspend {
                statsRepository.updateStats(GameStats(pairCount, 100, time, gamesPlayed = 2))
                leaderboardRepository.addEntry(any())
            }
        }

    @Test
    fun testInvoke_initialBestTimeZero() =
        runTest {
            val pairCount = 8
            val score = 50
            val time = 40L
            val moves = 20
            // bestTimeSeconds = 0 usually means not set or initial state
            val existingStats = GameStats(pairCount, 100, 0L, gamesPlayed = 1)

            everySuspend { statsRepository.getStatsForDifficulty(pairCount) } returns flowOf(existingStats)
            everySuspend { statsRepository.updateStats(any()) } returns Unit
            everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

            useCase(pairCount, score, time, moves, GameMode.TIME_ATTACK)

            verifySuspend {
                statsRepository.updateStats(GameStats(pairCount, 100, time, gamesPlayed = 2))
                leaderboardRepository.addEntry(any())
            }
        }

    @Test
    fun testInvoke_repositoryFailure_handlesException() =
        runTest {
            val pairCount = 8
            everySuspend { statsRepository.getStatsForDifficulty(any()) } throws RuntimeException("Repository Error")

            // Should not throw
            useCase(pairCount, 100, 60L, 20, GameMode.TIME_ATTACK)

            verifySuspend(VerifyMode.not) {
                statsRepository.updateStats(any())
                leaderboardRepository.addEntry(any())
            }
        }
}
