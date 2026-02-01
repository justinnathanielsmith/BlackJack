package io.github.smithjustinn.data.repositories

import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import io.github.smithjustinn.resources.Res
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
class JsonShopItemRepository(
    private val json: Json,
) : ShopItemRepository {
    override suspend fun getShopItems(): List<ShopItem> {
        val bytes = Res.readBytes("files/shop_items.json")
        val content = bytes.decodeToString()
        return json.decodeFromString(content)
    }
}
