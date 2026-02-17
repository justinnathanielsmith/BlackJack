package io.github.smithjustinn.domain.models

import kotlin.test.Test
import kotlin.test.assertFailsWith

class ShopItemTest {

    @Test
    fun `init should throw exception when price is negative`() {
        assertFailsWith<IllegalArgumentException> {
            ShopItem(
                id = "test_item",
                name = "Test Item",
                description = "Test Description",
                price = -100,
                type = ShopItemType.THEME
            )
        }
    }

    @Test
    fun `init should throw exception when name is blank`() {
        assertFailsWith<IllegalArgumentException> {
            ShopItem(
                id = "test_item",
                name = "   ",
                description = "Test Description",
                price = 100,
                type = ShopItemType.THEME
            )
        }
    }

    @Test
    fun `init should succeed with valid parameters`() {
        ShopItem(
            id = "test_item",
            name = "Test Item",
            description = "Test Description",
            price = 0,
            type = ShopItemType.THEME
        )
    }
}
