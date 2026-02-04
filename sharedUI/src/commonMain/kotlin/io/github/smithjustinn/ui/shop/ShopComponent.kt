package io.github.smithjustinn.ui.shop

import io.github.smithjustinn.domain.models.ShopItem
import kotlinx.coroutines.flow.StateFlow
import org.jetbrains.compose.resources.StringResource

sealed interface ShopErrorMessage {
    data class Resource(val res: StringResource) : ShopErrorMessage
    data class Message(val text: String) : ShopErrorMessage
}

data class ShopState(
    val balance: Long = 0,
    val items: List<ShopItem> = emptyList(),
    val unlockedItemIds: Set<String> = emptySet(),
    val activeThemeId: String? = null,
    val activeSkinId: String? = null,
    val error: ShopErrorMessage? = null,
)

interface ShopComponent {
    val state: StateFlow<ShopState>

    fun onBackClicked()

    fun onBuyItemClicked(item: ShopItem)

    fun onEquipItemClicked(item: ShopItem)

    fun onClearError()
}
