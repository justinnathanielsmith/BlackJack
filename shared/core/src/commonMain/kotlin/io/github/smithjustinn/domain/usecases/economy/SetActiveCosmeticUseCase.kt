package io.github.smithjustinn.domain.usecases.economy

import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository

class SetActiveCosmeticUseCase(
    private val playerEconomyRepository: PlayerEconomyRepository,
) {
    suspend operator fun invoke(
        itemId: String,
        itemType: ShopItemType,
    ) {
        when (itemType) {
            ShopItemType.THEME -> playerEconomyRepository.selectTheme(itemId)
            ShopItemType.CARD_SKIN -> playerEconomyRepository.selectSkin(itemId)
            ShopItemType.MUSIC -> playerEconomyRepository.selectMusic(itemId)
            ShopItemType.POWER_UP -> playerEconomyRepository.selectPowerUp(itemId)
        }
    }
}
