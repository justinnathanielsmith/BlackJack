package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.data.local.GameStateDao
import io.github.smithjustinn.data.local.GameStateEntity
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameStateRepositoryTest {
    private val dao = mock<GameStateDao>()
    private val json = Json { ignoreUnknownKeys = true }
    private val logger = Logger.withTag("Test")
    private val repository = GameStateRepositoryImpl(dao, json, logger)

    @Test
    fun testSaveGameState() =
        runTest {
            val state = MemoryGameState(pairCount = 8)
            everySuspend { dao.saveGameState(any()) } returns Unit

            val expectedEntity =
                GameStateEntity(
                    id = 0,
                    gameState = state,
                    elapsedTimeSeconds = 120,
                )

            repository.saveGameState(state, 120)

            verifySuspend {
                dao.saveGameState(expectedEntity)
            }
        }

    @Test
    fun testGetSavedGameState_found() =
        runTest {
            val state = MemoryGameState(pairCount = 8)
            val entity = GameStateEntity(id = 0, gameState = state, elapsedTimeSeconds = 300)
            everySuspend { dao.getSavedGameState() } returns entity

            val result = repository.getSavedGameState()
            assertNotNull(result)
            assertEquals(8, result.gameState.pairCount)
            assertEquals(300, result.elapsedTimeSeconds)
        }

    @Test
    fun testGetSavedGameState_notFound() =
        runTest {
            everySuspend { dao.getSavedGameState() } returns null
            val result = repository.getSavedGameState()
            assertNull(result)
        }

    @Test
    fun testClearSavedGameState() =
        runTest {
            everySuspend { dao.clearSavedGameState() } returns Unit
            repository.clearSavedGameState()
            verifySuspend { dao.clearSavedGameState() }
        }
}
