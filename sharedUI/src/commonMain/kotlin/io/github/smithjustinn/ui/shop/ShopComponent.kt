package io.github.smithjustinn.ui.shop

import io.github.smithjustinn.domain.models.ShopItem
import kotlinx.coroutines.flow.StateFlow

data class ShopState(
    val balance: Long = 0,
    val items: List<ShopItem> = emptyList(),
    val unlockedItemIds: Set<String> = emptySet(),
    val error: String? = null,
)

interface ShopComponent {
    val state: StateFlow<ShopState>

    fun onBackClicked()

    fun onBuyItemClicked(item: ShopItem)

    fun onClearError()
}
