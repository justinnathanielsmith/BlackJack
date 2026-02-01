package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository

class BuyItemUseCase(
    private val repository: PlayerEconomyRepository,
) {
    /**
     * Attempts to purchase an item.
     * @return Result.success if purchase successful, Result.failure if insufficient funds or other error.
     */
    suspend operator fun invoke(
        itemId: String,
        cost: Long,
    ): Result<Unit> {
        if (repository.isItemUnlocked(itemId)) {
            return Result.success(Unit) // Already owned
        }

        return if (repository.deductCurrency(cost)) {
            repository.unlockItem(itemId)
            Result.success(Unit)
        } else {
            Result.failure(Exception("Insufficient funds"))
        }
    }
}
