package io.github.smithjustinn.domain.usecases.stats

import co.touchlab.kermit.Logger
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
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
                statsRepository.updateStats(GameStats(pairCount, score, time))
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
            val existingStats = GameStats(pairCount, 100, 50L)

            everySuspend { statsRepository.getStatsForDifficulty(pairCount) } returns flowOf(existingStats)
            everySuspend { statsRepository.updateStats(any()) } returns Unit
            everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

            useCase(pairCount, score, time, moves, GameMode.TIME_ATTACK)

            verifySuspend {
                statsRepository.updateStats(GameStats(pairCount, 100, 50L))
            }
        }

    @Test
    fun testInvoke_error() =
        runTest {
            everySuspend { statsRepository.getStatsForDifficulty(any()) } throws RuntimeException("Error")
            useCase(8, 100, 60L, 20, GameMode.TIME_ATTACK)
        }
}
