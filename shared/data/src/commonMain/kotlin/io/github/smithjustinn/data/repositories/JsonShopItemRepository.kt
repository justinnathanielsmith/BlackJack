package io.github.smithjustinn.data.repositories

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.di.AppScope
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import io.github.smithjustinn.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@SingleIn(AppScope::class)
@ContributesBinding(AppScope::class)
@Inject
class JsonShopItemRepository(
    private val json: Json,
) : ShopItemRepository {
    private var cachedItems: List<ShopItem>? = null
    private val mutex = Mutex()

    override suspend fun getShopItems(): List<ShopItem> =
        mutex.withLock {
            cachedItems ?: withContext(Dispatchers.IO) {
                val bytes = Res.readBytes("files/shop_items.json")
                val content = bytes.decodeToString()
                json.decodeFromString<List<ShopItem>>(content).also {
                    cachedItems = it
                }
            }
        }
}
