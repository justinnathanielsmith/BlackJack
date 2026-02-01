package io.github.smithjustinn.domain.repositories

import kotlinx.coroutines.flow.StateFlow

interface PlayerEconomyRepository {
    val balance: StateFlow<Long>
    val unlockedItemIds: StateFlow<Set<String>>

    suspend fun addCurrency(amount: Long)

    suspend fun deductCurrency(amount: Long): Boolean

    suspend fun unlockItem(itemId: String)

    suspend fun isItemUnlocked(itemId: String): Boolean
}
