package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.repositories.ShopItemRepository

class BuyItemUseCase(
    private val repository: PlayerEconomyRepository,
    private val shopItemRepository: ShopItemRepository,
) {
    /**
     * Attempts to purchase an item.
     * @return Result.success if purchase successful, Result.failure if insufficient funds or other error.
     */
    suspend operator fun invoke(itemId: String): Result<Unit> =
        if (repository.isItemUnlocked(itemId)) {
            Result.success(Unit)
        } else {
            performPurchase(itemId)
        }

    private suspend fun performPurchase(itemId: String): Result<Unit> {
        val item =
            shopItemRepository.getShopItems().find { it.id == itemId }
                ?: return Result.failure(Exception("Item not found"))

        return if (repository.deductCurrency(item.price)) {
            repository.unlockItem(itemId)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Insufficient funds"))
        }
    }
}
