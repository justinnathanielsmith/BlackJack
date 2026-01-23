package io.github.smithjustinn.domain.usecases.stats

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import kotlinx.coroutines.flow.Flow

/**
 * Use case to retrieve game statistics for a specific difficulty.
 */
@Inject
open class GetGameStatsUseCase(
    private val gameStatsRepository: GameStatsRepository
) {
    open operator fun invoke(pairCount: Int): Flow<GameStats?> {
        return gameStatsRepository.getStatsForDifficulty(pairCount)
    }
}
