package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType

class GetShopItemsUseCase {
    operator fun invoke(): List<ShopItem> =
        listOf(
            ShopItem(
                id = "theme_classic",
                name = "Classic Theme",
                description = "Traditional card design.",
                price = 0, // Default
                type = ShopItemType.THEME,
            ),
            ShopItem(
                id = "theme_geometric",
                name = "Geometric Theme",
                description = "Modern geometric patterns.",
                price = 500,
                type = ShopItemType.THEME,
            ),
            ShopItem(
                id = "powerup_timebank",
                name = "Time Bank",
                description = "Add 10 seconds to the clock.",
                price = 200,
                type = ShopItemType.POWER_UP,
                isConsumable = true,
            ),
            ShopItem(
                id = "powerup_peek",
                name = "Pocket Aces",
                description = "Briefly reveal all cards.",
                price = 500,
                type = ShopItemType.POWER_UP,
                isConsumable = true,
            ),
        )
}
