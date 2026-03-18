package io.github.smithjustinn.domain.repositories

import app.cash.turbine.test
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.LeaderboardEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Instant

class FakeLeaderboardRepository : LeaderboardRepository {
    private val entriesFlow = MutableStateFlow<List<LeaderboardEntry>>(emptyList())

    override fun getTopEntries(
        pairCount: Int,
        gameMode: GameMode,
    ): Flow<List<LeaderboardEntry>> =
        entriesFlow.map { entries ->
            entries
                .filter { it.pairCount == pairCount && it.gameMode == gameMode }
                .sortedByDescending { it.score }
        }

    override fun getAllTopEntries(gameMode: GameMode): Flow<List<LeaderboardEntry>> =
        entriesFlow.map { entries ->
            entries
                .filter { it.gameMode == gameMode }
                .sortedByDescending { it.score }
        }

    override suspend fun addEntry(entry: LeaderboardEntry) {
        val current = entriesFlow.value.toMutableList()
        current.add(entry)
        entriesFlow.value = current
    }
}

class FakeLeaderboardRepositoryTest {
    @Test
    fun `getTopEntries returns empty list initially`() =
        runTest {
            val repository = FakeLeaderboardRepository()

            repository.getTopEntries(8, GameMode.TIME_ATTACK).test {
                assertTrue(awaitItem().isEmpty())
            }
        }

    @Test
    fun `getAllTopEntries returns empty list initially`() =
        runTest {
            val repository = FakeLeaderboardRepository()

            repository.getAllTopEntries(GameMode.TIME_ATTACK).test {
                assertTrue(awaitItem().isEmpty())
            }
        }

    @Test
    fun `addEntry and getTopEntries returns correctly filtered and sorted list`() =
        runTest {
            val repository = FakeLeaderboardRepository()

            val now = Instant.fromEpochSeconds(1000)
            val entry1 =
                LeaderboardEntry(
                    id = 1,
                    pairCount = 8,
                    score = 100,
                    timeSeconds = 60,
                    moves = 10,
                    timestamp = now,
                    gameMode = GameMode.TIME_ATTACK,
                )
            val entry2 =
                LeaderboardEntry(
                    id = 2,
                    pairCount = 8,
                    score = 200,
                    timeSeconds = 50,
                    moves = 15,
                    timestamp = now,
                    gameMode = GameMode.TIME_ATTACK,
                )
            val entry3 =
                LeaderboardEntry(
                    id = 3,
                    pairCount = 12,
                    score = 150,
                    timeSeconds = 80,
                    moves = 20,
                    timestamp = now,
                    gameMode = GameMode.TIME_ATTACK,
                )
            val entry4 =
                LeaderboardEntry(
                    id = 4,
                    pairCount = 8,
                    score = 300,
                    timeSeconds = 40,
                    moves = 5,
                    timestamp = now,
                    gameMode = GameMode.DAILY_CHALLENGE,
                )

            repository.getTopEntries(8, GameMode.TIME_ATTACK).test {
                assertTrue(awaitItem().isEmpty()) // initial state

                repository.addEntry(entry1)
                assertEquals(listOf(entry1), awaitItem())

                repository.addEntry(entry2)
                assertEquals(listOf(entry2, entry1), awaitItem()) // score 200 > 100

                repository.addEntry(entry3)
                // We just added entry3 but it doesn't match pairCount=8. Flow emits the same list.
                // The map operator does NOT dedupe by default.
                assertEquals(listOf(entry2, entry1), awaitItem())

                val entry5 =
                    LeaderboardEntry(
                        id = 5,
                        pairCount = 8,
                        score = 50,
                        timeSeconds = 70,
                        moves = 25,
                        timestamp = now,
                        gameMode = GameMode.TIME_ATTACK,
                    )
                repository.addEntry(entry5)
                assertEquals(listOf(entry2, entry1, entry5), awaitItem())

                repository.addEntry(entry4)
                assertEquals(listOf(entry2, entry1, entry5), awaitItem())
            }
        }

    @Test
    fun `getAllTopEntries returns all entries for the given game mode sorted by score`() =
        runTest {
            val repository = FakeLeaderboardRepository()

            val now = Instant.fromEpochSeconds(1000)
            val entry1 =
                LeaderboardEntry(
                    id = 1,
                    pairCount = 8,
                    score = 100,
                    timeSeconds = 60,
                    moves = 10,
                    timestamp = now,
                    gameMode = GameMode.TIME_ATTACK,
                )
            val entry2 =
                LeaderboardEntry(
                    id = 2,
                    pairCount = 12,
                    score = 200,
                    timeSeconds = 50,
                    moves = 15,
                    timestamp = now,
                    gameMode = GameMode.TIME_ATTACK,
                )
            val entry3 =
                LeaderboardEntry(
                    id = 3,
                    pairCount = 8,
                    score = 150,
                    timeSeconds = 80,
                    moves = 20,
                    timestamp = now,
                    gameMode = GameMode.DAILY_CHALLENGE,
                )

            repository.getAllTopEntries(GameMode.TIME_ATTACK).test {
                assertTrue(awaitItem().isEmpty()) // initial state

                repository.addEntry(entry1)
                assertEquals(listOf(entry1), awaitItem())

                repository.addEntry(entry2)
                assertEquals(listOf(entry2, entry1), awaitItem()) // score 200 > 100

                repository.addEntry(entry3)
                assertEquals(listOf(entry2, entry1), awaitItem())
            }
        }
}
