package io.github.smithjustinn.data.repositories

import co.touchlab.kermit.Logger
import io.github.smithjustinn.data.extensions.mapToStateFlow
import io.github.smithjustinn.data.local.PlayerEconomyDao
import io.github.smithjustinn.data.local.PlayerEconomyEntity
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMusic
import io.github.smithjustinn.domain.models.GamePowerUp
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.shareIn
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
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = 0L,
        ) { it?.balance ?: 0L }

    override val unlockedItemIds: StateFlow<Set<String>> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = PlayerEconomyEntity().unlockedItemIds.split(",").toSet(),
        ) { entity ->
            val idsString = entity?.unlockedItemIds ?: PlayerEconomyEntity().unlockedItemIds
            idsString
                .split(",")
                .filter { it.isNotBlank() }
                .toSet()
        }

    override val selectedTheme: StateFlow<CardBackTheme> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = CardBackTheme.GEOMETRIC,
        ) { entity ->
            val themeId = entity?.selectedThemeId ?: PlayerEconomyEntity().selectedThemeId
            CardBackTheme.fromIdOrName(themeId)
        }

    override val selectedThemeId: StateFlow<String> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = PlayerEconomyEntity().selectedThemeId,
        ) { entity ->
            entity?.selectedThemeId ?: PlayerEconomyEntity().selectedThemeId
        }

    override val selectedSkin: StateFlow<CardSymbolTheme> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = CardSymbolTheme.CLASSIC,
        ) { entity ->
            val skinId = entity?.selectedSkinId ?: PlayerEconomyEntity().selectedSkinId
            CardSymbolTheme.fromIdOrName(skinId)
        }

    override val selectedSkinId: StateFlow<String> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = PlayerEconomyEntity().selectedSkinId,
        ) { entity ->
            entity?.selectedSkinId ?: PlayerEconomyEntity().selectedSkinId
        }

    override val selectedMusic: StateFlow<GameMusic> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = GameMusic.DEFAULT,
        ) { entity ->
            val musicId = entity?.selectedMusicId ?: PlayerEconomyEntity().selectedMusicId
            GameMusic.fromIdOrName(musicId)
        }

    override val selectedMusicId: StateFlow<String> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = PlayerEconomyEntity().selectedMusicId,
        ) { entity ->
            entity?.selectedMusicId ?: PlayerEconomyEntity().selectedMusicId
        }

    override val selectedPowerUp: StateFlow<GamePowerUp> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = GamePowerUp.NONE,
        ) { entity ->
            val powerUpId = entity?.selectedPowerUpId ?: PlayerEconomyEntity().selectedPowerUpId
            GamePowerUp.fromIdOrName(powerUpId)
        }

    override val selectedPowerUpId: StateFlow<String> =
        economyFlow.mapToStateFlow(
            scope = scope,
            initialValue = PlayerEconomyEntity().selectedPowerUpId,
        ) { entity ->
            entity?.selectedPowerUpId ?: PlayerEconomyEntity().selectedPowerUpId
        }

    override suspend fun addCurrency(amount: Long) =
        writeMutex.withLock {
            require(amount >= 0) { "Amount must be non-negative" }
            val current = getOrCreateEntity()
            val newBalance =
                if (Long.MAX_VALUE - amount < current.balance) {
                    Long.MAX_VALUE
                } else {
                    current.balance + amount
                }
            dao.savePlayerEconomy(current.copy(balance = newBalance))
            logger.d { "Added $amount currency. New balance: $newBalance" }
        }

    override suspend fun deductCurrency(amount: Long): Boolean =
        writeMutex.withLock {
            require(amount >= 0) { "Amount must be non-negative" }
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

    override suspend fun isItemUnlocked(itemId: String): Boolean = unlockedItemIds.value.contains(itemId)

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

    override suspend fun selectMusic(musicId: String) =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            dao.savePlayerEconomy(current.copy(selectedMusicId = musicId))
            logger.d { "Selected music: $musicId" }
        }

    override suspend fun selectPowerUp(powerUpId: String) =
        writeMutex.withLock {
            val current = getOrCreateEntity()
            dao.savePlayerEconomy(current.copy(selectedPowerUpId = powerUpId))
            logger.d { "Selected power up: $powerUpId" }
        }

    private suspend fun getOrCreateEntity(): PlayerEconomyEntity =
        dao.getPlayerEconomy().firstOrNull() ?: PlayerEconomyEntity()
}
