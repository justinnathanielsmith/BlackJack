package io.github.smithjustinn.data.repositories

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Clock

class LeaderboardRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: LeaderboardRepositoryImpl

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        repository =
            LeaderboardRepositoryImpl(
                dao = database.leaderboardDao(),
                logger = Logger.withTag("Test"),
            )
    }

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun getTopEntries_emitsUpdates() =
        runTest {
            val pairCount = 8
            val gameMode = GameMode.TIME_ATTACK
            val entry1 =
                LeaderboardEntry(
                    pairCount = pairCount,
                    score = 1000,
                    timeSeconds = 60,
                    moves = 20,
                    timestamp = Clock.System.now(),
                    gameMode = gameMode,
                )

            val entry2 =
                LeaderboardEntry(
                    pairCount = pairCount,
                    score = 800,
                    timeSeconds = 70,
                    moves = 25,
                    timestamp = Clock.System.now(),
                    gameMode = gameMode,
                )

            repository.getTopEntries(pairCount, gameMode).test {
                assertEquals(emptyList(), awaitItem())

                repository.addEntry(entry1)
                val list1 = awaitItem()
                assertEquals(1, list1.size)
                assertEquals(entry1.score, list1[0].score)

                repository.addEntry(entry2)
                val list2 = awaitItem()
                assertEquals(2, list2.size)
                // Assuming DAO sorts by score DESC
                assertEquals(entry1.score, list2[0].score)
                assertEquals(entry2.score, list2[1].score)
            }
        }
}
