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
            val items = repository.getShopItems()
            assertTrue(items.isNotEmpty(), "Shop items should not be empty")

            val firstItem = items.first()
            assertEquals("theme_standard", firstItem.id)
            assertEquals("Standard Theme", firstItem.name)
            assertEquals(ShopItemType.THEME, firstItem.type)
        }

    @Test
    fun `getShopItems returns cached items on subsequent calls`() =
        runTest {
            val items1 = repository.getShopItems()
            val items2 = repository.getShopItems()

            // Check that the list instances are the same (identity equality)
            // This proves the cache was used.
            assertTrue(items1 === items2, "Subsequent calls should return the same cached list instance")
        }
}
