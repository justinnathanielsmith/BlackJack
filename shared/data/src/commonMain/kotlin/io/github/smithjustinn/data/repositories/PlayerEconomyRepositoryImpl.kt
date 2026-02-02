package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.local.PlayerEconomyDao
import io.github.smithjustinn.data.local.PlayerEconomyEntity
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class PlayerEconomyRepositoryImpl(
    private val dao: PlayerEconomyDao,
    private val logger: Logger,
    dispatchers: CoroutineDispatchers,
    private val scope: CoroutineScope = CoroutineScope(dispatchers.io + SupervisorJob()),
) : PlayerEconomyRepository {
    private val writeMutex = Mutex()

    private val economyFlow =
        dao
            .getPlayerEconomy()
            .shareIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                replay = 1,
            )

    init {
        scope.launch {
            seedIfNeeded()
        }
    }

    private suspend fun seedIfNeeded() {
        if (dao.getPlayerEconomy().firstOrNull() == null) {
            writeMutex.withLock {
                // Double check after lock
                if (dao.getPlayerEconomy().firstOrNull() == null) {
                    val defaultEntity = PlayerEconomyEntity()
                    dao.savePlayerEconomy(defaultEntity)
                    logger.d { "Seeded default player economy" }
                }
            }
        }
    }

    override val balance: StateFlow<Long> =
        economyFlow
            .map { it?.balance ?: 0L }
            .stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = 0L,
            )

    override val unlockedItemIds: StateFlow<Set<String>> =
        economyFlow
            .map { entity ->
                val idsString = entity?.unlockedItemIds ?: PlayerEconomyEntity().unlockedItemIds
                idsString
                    .split(",")
                    .filter { it.isNotBlank() }
                    .toSet()
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = PlayerEconomyEntity().unlockedItemIds.split(",").toSet(),
            )

    override val selectedTheme: StateFlow<CardBackTheme> =
        economyFlow
            .map { entity ->
                val themeId = entity?.selectedThemeId ?: PlayerEconomyEntity().selectedThemeId
                CardBackTheme.fromIdOrName(themeId)
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = CardBackTheme.GEOMETRIC,
            )

    override val selectedSkin: StateFlow<CardSymbolTheme> =
        economyFlow
            .map { entity ->
                val skinId = entity?.selectedSkinId ?: PlayerEconomyEntity().selectedSkinId
                CardSymbolTheme.fromIdOrName(skinId)
            }.stateIn(
                scope = scope,
                started = SharingStarted.Eagerly,
                initialValue = CardSymbolTheme.CLASSIC,
            )

    override suspend fun addCurrency(amount: Long) =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            val newBalance = current.balance + amount
            dao.savePlayerEconomy(current.copy(balance = newBalance))
            logger.d { "Added $amount currency. New balance: $newBalance" }
        }

    override suspend fun deductCurrency(amount: Long): Boolean =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            if (current.balance >= amount) {
                val newBalance = current.balance - amount
                dao.savePlayerEconomy(current.copy(balance = newBalance))
                logger.d { "Deducted $amount currency. New balance: $newBalance" }
                true
            } else {
                logger.w { "Insufficient funds. Balance: ${current.balance}, Required: $amount" }
                false
            }
        }

    override suspend fun unlockItem(itemId: String) =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            val currentItems =
                current.unlockedItemIds
                    .split(",")
                    .filter { it.isNotBlank() }
                    .toMutableSet()

            if (!currentItems.contains(itemId)) {
                currentItems.add(itemId)
                val newItemsString = currentItems.joinToString(",")
                dao.savePlayerEconomy(current.copy(unlockedItemIds = newItemsString))
                logger.d { "Unlocked item: $itemId" }
            }
        }

    override suspend fun isItemUnlocked(itemId: String): Boolean {
        val current = getOrCreateEntity()
        return current.unlockedItemIds
            .split(",")
            .filter { it.isNotBlank() }
            .contains(itemId)
    }

    override suspend fun selectTheme(themeId: String) =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            dao.savePlayerEconomy(current.copy(selectedThemeId = themeId))
            logger.d { "Selected theme: $themeId" }
        }

    override suspend fun selectSkin(skinId: String) =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            dao.savePlayerEconomy(current.copy(selectedSkinId = skinId))
            logger.d { "Selected skin: $skinId" }
        }

    private suspend fun getOrCreateEntity(): PlayerEconomyEntity =
        dao.getPlayerEconomy().firstOrNull() ?: PlayerEconomyEntity()
}
