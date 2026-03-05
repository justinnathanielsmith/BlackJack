package io.github.smithjustinn.domain.usecases.economy

import dev.zacsweers.metro.Inject
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.repositories.ShopItemRepository

@Inject
class GetShopItemsUseCase(
    private val shopItemRepository: ShopItemRepository,
) {
    suspend operator fun invoke(): List<ShopItem> = shopItemRepository.getShopItems()
}
