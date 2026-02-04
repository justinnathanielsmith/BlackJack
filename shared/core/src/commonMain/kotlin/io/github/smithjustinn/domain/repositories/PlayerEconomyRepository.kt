package io.github.smithjustinn.domain.repositories

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMusic
import io.github.smithjustinn.domain.models.GamePowerUp
import kotlinx.coroutines.flow.StateFlow

interface PlayerEconomyRepository {
    val balance: StateFlow<Long>
    val unlockedItemIds: StateFlow<Set<String>>
    val selectedTheme: StateFlow<CardBackTheme>
    val selectedThemeId: StateFlow<String>
    val selectedSkin: StateFlow<CardSymbolTheme>
    val selectedSkinId: StateFlow<String>
    val selectedMusic: StateFlow<GameMusic>
    val selectedMusicId: StateFlow<String>
    val selectedPowerUp: StateFlow<GamePowerUp>
    val selectedPowerUpId: StateFlow<String>

    suspend fun addCurrency(amount: Long)

    suspend fun deductCurrency(amount: Long): Boolean

    suspend fun unlockItem(itemId: String)

    suspend fun isItemUnlocked(itemId: String): Boolean

    suspend fun selectTheme(themeId: String)

    suspend fun selectSkin(skinId: String)

    suspend fun selectMusic(musicId: String)

    suspend fun selectPowerUp(powerUpId: String)
}
