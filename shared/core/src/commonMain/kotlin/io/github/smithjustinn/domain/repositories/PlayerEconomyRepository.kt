package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import kotlinx.coroutines.flow.StateFlow

interface PlayerEconomyRepository {
    val balance: StateFlow<Long>
    val unlockedItemIds: StateFlow<Set<String>>
    val selectedTheme: StateFlow<CardBackTheme>
    val selectedThemeId: StateFlow<String>
    val selectedSkin: StateFlow<CardSymbolTheme>
    val selectedSkinId: StateFlow<String>

    suspend fun addCurrency(amount: Long)

    suspend fun deductCurrency(amount: Long): Boolean

    suspend fun unlockItem(itemId: String)

    suspend fun isItemUnlocked(itemId: String): Boolean

    suspend fun selectTheme(themeId: String)

    suspend fun selectSkin(skinId: String)
}
