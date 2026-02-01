package io.github.smithjustinn.domain.models

enum class ShopItemType {
    THEME,
    MUSIC,
    POWER_UP,
}

data class ShopItem(
    val id: String,
    val name: String,
    val description: String,
    val price: Long,
    val type: ShopItemType,
    val isConsumable: Boolean = false,
)
