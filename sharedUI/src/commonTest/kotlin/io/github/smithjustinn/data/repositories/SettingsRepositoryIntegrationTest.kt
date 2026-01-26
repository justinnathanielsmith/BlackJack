package io.github.smithjustinn.data.repositories

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.domain.models.CardBackTheme
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SettingsRepositoryIntegrationTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: SettingsRepositoryImpl

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        repository =
            SettingsRepositoryImpl(
                dao = database.settingsDao(),
                logger = Logger.withTag("Test"),
            )
    }

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun testSettingsUpdates() =
        runTest {
            // Test Peek Enabled
            repository.isPeekEnabled.test {
                assertTrue(awaitItem()) // Default
                repository.setPeekEnabled(false)
                assertFalse(awaitItem())
            }

            // Test Sound Enabled
            repository.isSoundEnabled.test {
                assertTrue(awaitItem()) // Default
                repository.setSoundEnabled(false)
                assertFalse(awaitItem())
            }

            // Test Sound Volume
            repository.soundVolume.test {
                assertEquals(1.0f, awaitItem()) // Default
                repository.setSoundVolume(0.5f)
                assertEquals(0.5f, awaitItem())
            }

            // Test Card Back Theme
            repository.cardBackTheme.test {
                assertEquals(CardBackTheme.GEOMETRIC, awaitItem()) // Default
                repository.setCardBackTheme(CardBackTheme.PATTERN)
                assertEquals(CardBackTheme.PATTERN, awaitItem())
            }

            // Test walkthrough
            repository.isWalkthroughCompleted.test {
                assertFalse(awaitItem())
                repository.setWalkthroughCompleted(true)
                assertTrue(awaitItem())
            }
        }
}
