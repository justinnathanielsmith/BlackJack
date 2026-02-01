package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.domain.models.ShopItemType
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class JsonShopItemRepositoryTest {
    private val json = Json { ignoreUnknownKeys = true }
    private val repository = JsonShopItemRepository(json)

    @Test
    fun `getShopItems returns parsed items from json`() =
        runTest {
            // This test relies on the actual resources being available to the test environment.
            // If this fails due to MissingResourceException, it means the test setup requires
            // additional configuration to expose composeResources to commonTest.

            // However, we can attempt it.
            try {
                val items = repository.getShopItems()
                assertTrue(items.isNotEmpty(), "Shop items should not be empty")

                val firstItem = items.first()
                assertEquals("theme_classic", firstItem.id)
                assertEquals("Classic Theme", firstItem.name)
                assertEquals(ShopItemType.THEME, firstItem.type)
            } catch (e: Exception) {
                // Fallback: If resource loading fails in unit test environment is a known issue
                // without specific gradle setup, we might skip or fail.
                // For now, let's fail to see if it works.
                throw e
            }
        }
}
