package io.github.smithjustinn.domain.repositories

import app.cash.turbine.test
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FakeDailyChallengeRepository : DailyChallengeRepository {
    private val completedDates = MutableStateFlow<Set<Long>>(emptySet())

    override fun isChallengeCompleted(date: Long): Flow<Boolean> =
        completedDates.map { it.contains(date) }

    override suspend fun saveChallengeResult(
        date: Long,
        score: Int,
        timeSeconds: Long,
        moves: Int,
    ) {
        val current = completedDates.value.toMutableSet()
        current.add(date)
        completedDates.value = current
    }
}

class FakeDailyChallengeRepositoryTest {
    @Test
    fun `isChallengeCompleted returns false initially`() =
        runTest {
            val repository = FakeDailyChallengeRepository()

            repository.isChallengeCompleted(123456789L).test {
                assertFalse(awaitItem())
            }
        }

    @Test
    fun `saveChallengeResult saves data correctly and isChallengeCompleted returns true`() =
        runTest {
            val repository = FakeDailyChallengeRepository()
            val date = 123456789L

            repository.isChallengeCompleted(date).test {
                assertFalse(awaitItem()) // initial state

                repository.saveChallengeResult(date, 100, 60, 20)
                assertTrue(awaitItem())
            }
        }
}
