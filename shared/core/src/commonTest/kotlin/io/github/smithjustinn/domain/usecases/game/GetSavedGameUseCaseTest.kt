package io.github.smithjustinn.domain.usecases.game

import co.touchlab.kermit.Logger
import dev.mokkery.answering.returns
import dev.mokkery.answering.throws
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.domain.repositories.GameStateRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class GetSavedGameUseCaseTest {
    private val repository = mock<GameStateRepository>()
    private val logger = Logger.withTag("Test")
    private val useCase = GetSavedGameUseCase(repository, logger)

    @Test
    fun invoke_returnsSavedGame_whenRepositorySucceeds() =
        runTest {
            val state = MemoryGameState()
            val expectedSavedGame = SavedGame(state, 100L)
            everySuspend { repository.getSavedGameState() } returns expectedSavedGame

            val result = useCase()

            assertEquals(state, result?.gameState)
            assertEquals(100L, result?.elapsedTimeSeconds)
        }

    @Test
    fun invoke_returnsNull_whenRepositoryThrowsException() =
        runTest {
            everySuspend { repository.getSavedGameState() } throws RuntimeException("DB Error")

            val result = useCase()

            assertEquals(null, result)
        }
}
