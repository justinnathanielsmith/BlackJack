package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMusic
import io.github.smithjustinn.domain.models.GamePowerUp
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository

class SetActiveCosmeticUseCase(
    private val playerEconomyRepository: PlayerEconomyRepository,
) {
    suspend operator fun invoke(
        itemId: String,
        itemType: ShopItemType,
    ) {
        val isDefault =
            when (itemType) {
                ShopItemType.THEME -> itemId == CardBackTheme.GEOMETRIC.id
                ShopItemType.CARD_SKIN -> itemId == CardSymbolTheme.CLASSIC.id
                ShopItemType.MUSIC -> itemId == GameMusic.DEFAULT.id
                ShopItemType.POWER_UP -> itemId == GamePowerUp.NONE.id
                ShopItemType.FEATURE -> false
            }

        // SECURITY: Prevent unauthorized equipping of locked items
        require(isDefault || playerEconomyRepository.isItemUnlocked(itemId)) { "Item not unlocked: $itemId" }

        when (itemType) {
            ShopItemType.THEME -> playerEconomyRepository.selectTheme(itemId)
            ShopItemType.CARD_SKIN -> playerEconomyRepository.selectSkin(itemId)
            ShopItemType.MUSIC -> playerEconomyRepository.selectMusic(itemId)
            ShopItemType.POWER_UP -> playerEconomyRepository.selectPowerUp(itemId)
            ShopItemType.FEATURE -> { /* Features are not equipped, they are just unlocked */ }
        }
    }
}
