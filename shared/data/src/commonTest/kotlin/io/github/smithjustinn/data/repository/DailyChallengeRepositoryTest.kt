package io.github.smithjustinn.data.repository

import app.cash.turbine.test
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.data.local.DailyChallengeDao
import io.github.smithjustinn.data.local.DailyChallengeEntity
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class DailyChallengeRepositoryTest {
    private val dao = mock<DailyChallengeDao>()
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = CoroutineDispatchers(
        main = testDispatcher,
        mainImmediate = testDispatcher,
        io = testDispatcher,
        default = testDispatcher
    )
    private val repository = DailyChallengeRepositoryImpl(dao, dispatchers)

    @Test
    fun testIsChallengeCompleted_true() =
        runTest {
            val date = 123456789L
            val entity = DailyChallengeEntity(date, true, 100, 60, 20)
            every { dao.getDailyChallenge(date) } returns flowOf(entity)

            repository.isChallengeCompleted(date).test {
                assertEquals(true, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun testIsChallengeCompleted_false() =
        runTest {
            val date = 123456789L
            val entity = DailyChallengeEntity(date, false, 0, 0, 0)
            every { dao.getDailyChallenge(date) } returns flowOf(entity)

            repository.isChallengeCompleted(date).test {
                assertEquals(false, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun testIsChallengeCompleted_null() =
        runTest {
            val date = 123456789L
            every { dao.getDailyChallenge(date) } returns flowOf(null)

            repository.isChallengeCompleted(date).test {
                assertEquals(false, awaitItem())
                awaitComplete()
            }
        }

    @Test
    fun testSaveChallengeResult() =
        runTest {
            val date = 123456789L
            everySuspend { dao.insertOrUpdate(any<DailyChallengeEntity>()) } returns Unit

            repository.saveChallengeResult(date, 100, 60, 20)

            val expectedEntity =
                DailyChallengeEntity(
                    date = date,
                    isCompleted = true,
                    score = 100,
                    timeSeconds = 60,
                    moves = 20,
                )
            verifySuspend {
                dao.insertOrUpdate(expectedEntity)
            }
        }
}
