package io.github.smithjustinn.ui.shop

sealed class ShopItemState {
    data class Locked(
        val price: Long,
        val canAfford: Boolean,
    ) : ShopItemState()

    data object Owned : ShopItemState()

    data object Equipped : ShopItemState()

    data object UnlockedFeature : ShopItemState()
}
