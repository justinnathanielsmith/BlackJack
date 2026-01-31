package io.github.smithjustinn.domain.usecases.stats

import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetGameStatsUseCaseTest {
    private val repository = mock<GameStatsRepository>()
    private val useCase = GetGameStatsUseCase(repository)

    @Test
    fun `invoke returns stats from repository`() = runTest {
        val stats = GameStats(pairCount = 8, bestScore = 100, bestTimeSeconds = 60)
        every { repository.getStatsForDifficulty(8) } returns flowOf(stats)

        val result = useCase(8).first()

        assertEquals(stats, result)
    }
}
