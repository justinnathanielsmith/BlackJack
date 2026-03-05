package io.github.smithjustinn.ui.shop

import com.arkivanov.decompose.ComponentContext
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.ad_unit_id
import io.github.smithjustinn.resources.shop_purchase_failed
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock

class DefaultShopComponent(
    componentContext: ComponentContext,
    private val appGraph: AppGraph,
    private val onBackClicked: () -> Unit,
) : ShopComponent,
    ComponentContext by componentContext {
    // All dependencies now come from AppGraph (Metro DI)
    private val buyItemUseCase = appGraph.buyItemUseCase
    private val getPlayerBalanceUseCase = appGraph.getPlayerBalanceUseCase
    private val getShopItemsUseCase = appGraph.getShopItemsUseCase
    private val setActiveCosmeticUseCase = appGraph.setActiveCosmeticUseCase
    private val playerEconomyRepository = appGraph.playerEconomyRepository
    private val hapticsService = appGraph.hapticsService

    private val _state = MutableStateFlow(ShopState())
    override val state: StateFlow<ShopState> = _state.asStateFlow()

    private var lastAdShownTimestamp = 0L

    private val scope = lifecycle.componentScope(appGraph.coroutineDispatchers.mainImmediate)

    init {
        // Initial ad availability
        _state.update { it.copy(isRewardedAdAvailable = true) }

        scope.launch {
            val balanceFlow = getPlayerBalanceUseCase()
            val unlockedFlow = playerEconomyRepository.unlockedItemIds
            val themeIdFlow = playerEconomyRepository.selectedThemeId
            val skinIdFlow = playerEconomyRepository.selectedSkinId
            val allItems =
                withContext(appGraph.coroutineDispatchers.io) {
                    getShopItemsUseCase()
                }

            combine(balanceFlow, unlockedFlow, themeIdFlow, skinIdFlow) { balance, unlockedIds, themeId, skinId ->
                ShopState(
                    balance = balance,
                    items = allItems,
                    unlockedItemIds = unlockedIds,
                    activeThemeId = themeId,
                    activeSkinId = skinId,
                )
            }.collect { newState ->
                _state.update {
                    it.copy(
                        balance = newState.balance,
                        items = newState.items,
                        unlockedItemIds = newState.unlockedItemIds,
                        activeThemeId = newState.activeThemeId,
                        activeSkinId = newState.activeSkinId,
                    )
                }
            }
        }
    }

    override fun onBackClicked() {
        onBackClicked.invoke()
    }

    override fun onBuyItemClicked(item: ShopItem) {
        scope.launch {
            val result = buyItemUseCase(item.id)
            if (result.isFailure) {
                // Show error
                val error =
                    result.exceptionOrNull()?.message?.let {
                        ShopErrorMessage.Message(it)
                    } ?: ShopErrorMessage.Resource(Res.string.shop_purchase_failed)
                _state.update { it.copy(error = error) }
            } else {
                // Success handled by flow update
                hapticsService.performHapticFeedback(HapticFeedbackType.LONG_PRESS)
            }
        }
    }

    override fun onEquipItemClicked(item: ShopItem) {
        scope.launch {
            setActiveCosmeticUseCase(item.id, item.type)
        }
    }

    override fun onClearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onWatchAdForRewardClicked() {
        if (!_state.value.isRewardedAdAvailable) return

        appGraph.adService.showRewardedAd {
            scope.launch {
                appGraph.earnCurrencyUseCase.execute(REWARDED_AD_CHIPS_AMOUNT)
                hapticsService.performHapticFeedback(HapticFeedbackType.LONG_PRESS)

                // Update cooldown
                lastAdShownTimestamp = Clock.System.now().toEpochMilliseconds()
                updateAdAvailability()

                // Reload ad for next time
                // Handle Headless JVM mode correctly for tests.
                val adUnitId =
                    try {
                        getString(Res.string.ad_unit_id)
                    } catch (
                        @Suppress("SwallowedException", "TooGenericExceptionCaught") e: Exception,
                    ) {
                        "test_ad_unit_id"
                    }
                appGraph.adService.loadRewardedAd(adUnitId)
            }
        }
    }

    private fun updateAdAvailability() {
        val now = Clock.System.now().toEpochMilliseconds()
        val timeSinceLastAd = now - lastAdShownTimestamp
        val isAvailable = timeSinceLastAd >= AD_COOLDOWN_MILLIS
        _state.update { it.copy(isRewardedAdAvailable = isAvailable) }

        if (!isAvailable) {
            // Schedule re-check when cooldown expires
            scope.launch {
                val remainingTime = AD_COOLDOWN_MILLIS - timeSinceLastAd
                delay(remainingTime)
                _state.update { it.copy(isRewardedAdAvailable = true) }
            }
        }
    }

    companion object {
        private const val REWARDED_AD_CHIPS_AMOUNT = 200L
        private const val AD_COOLDOWN_MILLIS = 30 * 1000L // 30 seconds
    }
}
