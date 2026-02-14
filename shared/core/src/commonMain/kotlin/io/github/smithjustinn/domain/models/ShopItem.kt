package io.github.smithjustinn.domain.models

import kotlinx.serialization.Serializable

@Serializable
enum class ShopItemType {
    THEME,
    CARD_SKIN,
    MUSIC,
    POWER_UP,
    FEATURE,
}

@Serializable
data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
    val type: ShopItemType,
    val isConsumable: Boolean = false,
    val hexColor: String? = null,
    val isVisible: Boolean = true,
)
