package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class DailyChallengeRepositorySecurityTest {
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
    fun saveChallengeResult_throws_exception_for_negative_score() =
        runTest(testDispatcher) {
            val date = 123456789L
            assertFailsWith<IllegalArgumentException> {
                repository.saveChallengeResult(date, -1, 60L, 20)
            }
        }

    @Test
    fun saveChallengeResult_throws_exception_for_negative_time() =
        runTest(testDispatcher) {
            val date = 123456789L
            assertFailsWith<IllegalArgumentException> {
                repository.saveChallengeResult(date, 100, -1L, 20)
            }
        }

    @Test
    fun saveChallengeResult_throws_exception_for_negative_moves() =
        runTest(testDispatcher) {
            val date = 123456789L
            assertFailsWith<IllegalArgumentException> {
                repository.saveChallengeResult(date, 100, 60L, -1)
            }
        }

    @Test
    fun saveChallengeResult_throws_exception_for_non_positive_date() =
        runTest(testDispatcher) {
            assertFailsWith<IllegalArgumentException> {
                repository.saveChallengeResult(0L, 100, 60L, 20)
            }
            assertFailsWith<IllegalArgumentException> {
                repository.saveChallengeResult(-1L, 100, 60L, 20)
            }
        }
}
