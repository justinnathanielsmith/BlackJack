package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class DailyChallengeRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: DailyChallengeRepositoryImpl
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        val dispatchers =
            CoroutineDispatchers(
                main = testDispatcher,
                mainImmediate = testDispatcher,
                io = testDispatcher,
                default = testDispatcher,
            )
        repository =
            DailyChallengeRepositoryImpl(
                dao = database.dailyChallengeDao(),
                dispatchers = dispatchers,
            )
    }

    @AfterTest
    fun tearDown() {
        database.close()
    }

    @Test
    fun isChallengeCompleted_returns_false_initially() =
        runTest(testDispatcher) {
            val date = 123456789L
            val isCompleted = repository.isChallengeCompleted(date).first()
            assertFalse(isCompleted)
        }

    @Test
    fun saveChallengeResult_saves_data_correctly() =
        runTest(testDispatcher) {
            val date = 123456789L
            val score = 100
            val timeSeconds = 60L
            val moves = 20

            repository.saveChallengeResult(date, score, timeSeconds, moves)

            val entity = database.dailyChallengeDao().getDailyChallenge(date).first()
            assertEquals(score, entity?.score)
            assertEquals(timeSeconds, entity?.timeSeconds)
            assertEquals(moves, entity?.moves)
            assertEquals(true, entity?.isCompleted)
            assertEquals(date, entity?.date)
        }

    @Test
    fun isChallengeCompleted_returns_true_after_completion() =
        runTest(testDispatcher) {
            val date = 987654321L
            // Pre-populate data via repository
            repository.saveChallengeResult(date, 50, 30L, 10)

            val isCompleted = repository.isChallengeCompleted(date).first()
            assertTrue(isCompleted)
        }
}
