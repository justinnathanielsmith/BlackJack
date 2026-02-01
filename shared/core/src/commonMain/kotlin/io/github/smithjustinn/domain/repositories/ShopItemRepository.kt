package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.ShopItem

interface ShopItemRepository {
    suspend fun getShopItems(): List<ShopItem>
}
