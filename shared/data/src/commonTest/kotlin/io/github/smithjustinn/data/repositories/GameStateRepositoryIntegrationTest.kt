package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class GameStateRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: GameStateRepositoryImpl

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        repository =
            GameStateRepositoryImpl(
                dao = database.gameStateDao(),
                json = Json,
                logger = Logger.withTag("Test"),
            )
    }

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun saveAndGetGameState() =
        runTest {
            val gameState =
                MemoryGameState(
                    pairCount = 8,
                    mode = GameMode.TIME_ATTACK,
                )
            val elapsedTime = 120L

            repository.saveGameState(gameState, elapsedTime)

            val retrieved = repository.getSavedGameState()
            assertNotNull(retrieved)
            assertEquals(gameState.pairCount, retrieved.first.pairCount)
            assertEquals(gameState.mode, retrieved.first.mode)
            assertEquals(elapsedTime, retrieved.second)
        }

    @Test
    fun clearGameState() =
        runTest {
            val gameState = MemoryGameState(pairCount = 8, mode = GameMode.TIME_ATTACK)
            repository.saveGameState(gameState, 100L)

            repository.clearSavedGameState()

            val retrieved = repository.getSavedGameState()
            assertNull(retrieved)
        }
}
