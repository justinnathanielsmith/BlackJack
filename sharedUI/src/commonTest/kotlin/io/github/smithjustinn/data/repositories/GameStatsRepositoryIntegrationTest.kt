package io.github.smithjustinn.data.repositories

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.domain.models.GameStats
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GameStatsRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: GameStatsRepositoryImpl

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        repository = GameStatsRepositoryImpl(
            dao = database.gameStatsDao(),
            logger = Logger.withTag("Test"),
        )
    }

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun getStatsForDifficulty_emitsUpdates() = runTest {
        val pairCount = 8
        val stats = GameStats(pairCount = pairCount, bestScore = 1000, bestTimeSeconds = 60)

        repository.getStatsForDifficulty(pairCount).test {
            assertEquals(null, awaitItem())

            repository.updateStats(stats)
            assertEquals(stats, awaitItem())

            val updatedStats = stats.copy(bestScore = 1200)
            repository.updateStats(updatedStats)
            assertEquals(updatedStats, awaitItem())
        }
    }
}
