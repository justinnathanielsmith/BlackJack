package io.github.smithjustinn.domain.repositories

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.GameStats
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class FakeGameStatsRepository : GameStatsRepository {
    private val statsFlow = MutableStateFlow<Map<Int, GameStats>>(emptyMap())

    override fun getStatsForDifficulty(pairCount: Int): Flow<GameStats?> = statsFlow.map { it[pairCount] }

    override fun getAllStats(): Flow<List<GameStats>> = statsFlow.map { it.values.toList() }

    override suspend fun updateStats(stats: GameStats) {
        val current = statsFlow.value.toMutableMap()
        current[stats.pairCount] = stats
        statsFlow.value = current
    }
}

class FakeGameStatsRepositoryTest {
    @Test
    fun `getStatsForDifficulty returns null initially`() =
        runTest {
            val repository = FakeGameStatsRepository()

            repository.getStatsForDifficulty(8).test {
                assertNull(awaitItem())
            }
        }

    @Test
    fun `updateStats saves stats and getStatsForDifficulty returns them`() =
        runTest {
            val repository = FakeGameStatsRepository()
            val stats = GameStats(pairCount = 8, bestScore = 100, bestTimeSeconds = 60, gamesPlayed = 1)

            repository.getStatsForDifficulty(8).test {
                assertNull(awaitItem()) // initial state

                repository.updateStats(stats)
                assertEquals(stats, awaitItem())
            }
        }

    @Test
    fun `getAllStats returns empty list initially`() =
        runTest {
            val repository = FakeGameStatsRepository()

            repository.getAllStats().test {
                assertEquals(emptyList(), awaitItem())
            }
        }

    @Test
    fun `getAllStats returns all updated stats`() =
        runTest {
            val repository = FakeGameStatsRepository()
            val stats8 = GameStats(pairCount = 8, bestScore = 100, bestTimeSeconds = 60, gamesPlayed = 1)
            val stats12 = GameStats(pairCount = 12, bestScore = 200, bestTimeSeconds = 120, gamesPlayed = 2)

            repository.getAllStats().test {
                assertEquals(emptyList(), awaitItem()) // initial state

                repository.updateStats(stats8)
                assertEquals(listOf(stats8), awaitItem())

                repository.updateStats(stats12)
                assertEquals(listOf(stats8, stats12), awaitItem())
            }
        }

    @Test
    fun `updateStats overwrites existing stats for the same difficulty`() =
        runTest {
            val repository = FakeGameStatsRepository()
            val initialStats = GameStats(pairCount = 8, bestScore = 100, bestTimeSeconds = 60, gamesPlayed = 1)
            val updatedStats = GameStats(pairCount = 8, bestScore = 150, bestTimeSeconds = 50, gamesPlayed = 2)

            repository.getStatsForDifficulty(8).test {
                assertNull(awaitItem()) // initial state

                repository.updateStats(initialStats)
                assertEquals(initialStats, awaitItem())

                repository.updateStats(updatedStats)
                assertEquals(updatedStats, awaitItem())
            }
        }
}
