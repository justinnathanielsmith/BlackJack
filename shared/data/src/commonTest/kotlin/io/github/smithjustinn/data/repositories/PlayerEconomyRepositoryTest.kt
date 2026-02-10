package io.github.smithjustinn.data.repositories

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.AppDatabase
import io.github.smithjustinn.data.local.createTestDatabase
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMusic
import io.github.smithjustinn.domain.models.GamePowerUp
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerEconomyRepositoryTest {
    private lateinit var database: AppDatabase
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setup() {
        database = createTestDatabase()
        // We will initialize the repository in each test to use the appropriate TestScope/backgroundScope
    }

    private fun createRepository(scope: CoroutineScope): PlayerEconomyRepositoryImpl =
        PlayerEconomyRepositoryImpl(
            dao = database.playerEconomyDao(),
            logger = Logger.withTag("EconomyTest"),
            dispatchers =
                CoroutineDispatchers(
                    main = testDispatcher,
                    mainImmediate = testDispatcher,
                    io = testDispatcher,
                    default = testDispatcher,
                ),
            scope = scope,
        )

    @AfterTest
    fun cleanup() {
        database.close()
    }

    @Test
    fun addCurrency_increasesBalance() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.balance.test {
                assertEquals(0L, awaitItem())

                repository.addCurrency(100L)
                assertEquals(100L, awaitItem())

                repository.addCurrency(50L)
                assertEquals(150L, awaitItem())
            }
        }

    @Test
    fun deductCurrency_withSufficientFunds_decreasesBalance() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.balance.test {
                assertEquals(0L, awaitItem())

                repository.addCurrency(100L)
                assertEquals(100L, awaitItem())

                val result = repository.deductCurrency(40L)
                assertTrue(result)
                assertEquals(60L, awaitItem())
            }
        }

    @Test
    fun deductCurrency_withInsufficientFunds_returnsFalseAndDoesNotChangeBalance() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.balance.test {
                assertEquals(0L, awaitItem())

                repository.addCurrency(50L)
                assertEquals(50L, awaitItem())

                val result = repository.deductCurrency(100L)
                assertFalse(result)
                expectNoEvents()
            }
        }

    @Test
    fun unlockItem_addsItemToSet() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.unlockedItemIds.test {
                val initial = awaitItem()
                assertTrue(initial.contains("theme_standard"))
                assertTrue(initial.contains("skin_classic"))

                repository.unlockItem("item_1")
                assertTrue(awaitItem().contains("item_1"))

                repository.unlockItem("item_2")
                assertTrue(awaitItem().contains("item_2"))
            }
        }

    @Test
    fun unlockItem_alreadyUnlockedItem_doesNotDuplicate() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.unlockedItemIds.test {
                val initialItems = awaitItem()
                assertTrue(initialItems.contains("theme_standard"))
                assertTrue(initialItems.contains("skin_classic"))

                repository.unlockItem("item_1")
                assertTrue(awaitItem().contains("item_1"))

                repository.unlockItem("item_1")
                expectNoEvents()
            }
        }

    @Test
    fun isItemUnlocked_returnsCorrectValue() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            assertFalse(repository.isItemUnlocked("item_1"))

            repository.unlockItem("item_1")
            // Wait for propagation
            repository.unlockedItemIds.first { it.contains("item_1") }
            assertTrue(repository.isItemUnlocked("item_1"))
        }

    @Test
    fun persistenceAcrossInstances_preservesBalanceAndItems() =
        runTest(testDispatcher) {
            // First instance
            val repo1 = createRepository(backgroundScope)
            repo1.addCurrency(200L)
            repo1.unlockItem("perm_item_1")

            // Second instance (app "restart")
            val repo2 = createRepository(backgroundScope)
            repo2.balance.test {
                // Skip initial value 0L if it appears first, otherwise expect 200L
                val first = awaitItem()
                if (first == 0L) {
                    assertEquals(200L, awaitItem())
                } else {
                    assertEquals(200L, first)
                }
            }
            // Wait for propagation
            repo2.unlockedItemIds.first { it.contains("perm_item_1") }
            assertTrue(repo2.isItemUnlocked("perm_item_1"))
        }

    @Test
    fun themeSelectionPersistence_preservesSelectedTheme() =
        runTest(testDispatcher) {
            val repo1 = createRepository(backgroundScope)
            repo1.selectTheme(CardBackTheme.CLASSIC.id)

            // Second instance
            val repo2 = createRepository(backgroundScope)
            repo2.selectedTheme.test {
                val first = awaitItem()
                if (first == CardBackTheme.GEOMETRIC) {
                    assertEquals(CardBackTheme.CLASSIC, awaitItem())
                } else {
                    assertEquals(CardBackTheme.CLASSIC, first)
                }
            }
        }

    @Test
    fun skinSelectionPersistence_preservesSelectedSkin() =
        runTest(testDispatcher) {
            val repo1 = createRepository(backgroundScope)
            repo1.selectSkin(CardSymbolTheme.MINIMAL.id)

            // Second instance
            val repo2 = createRepository(backgroundScope)
            repo2.selectedSkin.test {
                val first = awaitItem()
                if (first == CardSymbolTheme.CLASSIC) {
                    assertEquals(CardSymbolTheme.MINIMAL, awaitItem())
                } else {
                    assertEquals(CardSymbolTheme.MINIMAL, first)
                }
            }
        }

    @Test
    fun musicSelectionPersistence_preservesSelectedMusic() =
        runTest(testDispatcher) {
            val repo1 = createRepository(backgroundScope)
            repo1.selectMusic(GameMusic.DEFAULT.id)

            // Second instance
            val repo2 = createRepository(backgroundScope)
            repo2.selectedMusic.test {
                assertEquals(GameMusic.DEFAULT, awaitItem())
            }
        }

    @Test
    fun powerUpSelectionPersistence_preservesSelectedPowerUp() =
        runTest(testDispatcher) {
            val repo1 = createRepository(backgroundScope)
            repo1.selectPowerUp(GamePowerUp.TIME_BANK.id)

            // Second instance
            val repo2 = createRepository(backgroundScope)
            repo2.selectedPowerUp.test {
                val first = awaitItem()
                if (first == GamePowerUp.NONE) {
                    assertEquals(GamePowerUp.TIME_BANK, awaitItem())
                } else {
                    assertEquals(GamePowerUp.TIME_BANK, first)
                }
            }
        }

    @Test
    fun testDefaultSeeding() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.unlockedItemIds.test {
                val items = awaitItem()
                assertTrue(items.contains("theme_standard"), "Should contain theme_standard")
                assertTrue(items.contains("skin_classic"), "Should contain skin_classic")
                assertEquals(2, items.size)
            }
            repository.balance.test {
                assertEquals(0L, awaitItem())
            }
            repository.selectedTheme.test {
                assertEquals(CardBackTheme.GEOMETRIC, awaitItem())
            }
            repository.selectedSkin.test {
                assertEquals(CardSymbolTheme.CLASSIC, awaitItem())
            }
        }

    @Test
    fun themeSelection_handlesLegacyNames() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            repository.selectTheme("CLASSIC") // Old name format
            repository.selectedTheme.test {
                val first = awaitItem()
                if (first == CardBackTheme.GEOMETRIC) {
                    assertEquals(CardBackTheme.CLASSIC, awaitItem())
                } else {
                    assertEquals(CardBackTheme.CLASSIC, first)
                }
            }

            repository.selectSkin("MINIMAL") // Old name format
            repository.selectedSkin.test {
                val first = awaitItem()
                if (first == CardSymbolTheme.CLASSIC) {
                    assertEquals(CardSymbolTheme.MINIMAL, awaitItem())
                } else {
                    assertEquals(CardSymbolTheme.MINIMAL, first)
                }
            }
        }

    @Test
    fun addCurrency_throwsExceptionForNegativeAmount() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            assertFailsWith<IllegalArgumentException> {
                repository.addCurrency(-100L)
            }
        }

    @Test
    fun deductCurrency_throwsExceptionForNegativeAmount() =
        runTest(testDispatcher) {
            val repository = createRepository(backgroundScope)
            assertFailsWith<IllegalArgumentException> {
                repository.deductCurrency(-100L)
            }
        }
}
