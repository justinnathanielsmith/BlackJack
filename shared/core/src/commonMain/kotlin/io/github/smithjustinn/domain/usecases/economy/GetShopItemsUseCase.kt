package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.repositories.ShopItemRepository

class GetShopItemsUseCase(
    private val shopItemRepository: ShopItemRepository,
) {
    suspend operator fun invoke(): List<ShopItem> = shopItemRepository.getShopItems()
}
