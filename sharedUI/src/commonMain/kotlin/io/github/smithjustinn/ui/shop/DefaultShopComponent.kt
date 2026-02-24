package io.github.smithjustinn.ui.shop

import com.arkivanov.decompose.ComponentContext
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.usecases.economy.BuyItemUseCase
import io.github.smithjustinn.domain.usecases.economy.GetPlayerBalanceUseCase
import io.github.smithjustinn.domain.usecases.economy.GetShopItemsUseCase
import io.github.smithjustinn.domain.usecases.economy.SetActiveCosmeticUseCase
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.ad_unit_id
import io.github.smithjustinn.resources.shop_purchase_failed
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.services.HapticsService
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
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import kotlin.time.Clock

class DefaultShopComponent(
    componentContext: ComponentContext,
    private val appGraph: AppGraph, // Using AppGraph for injection if possible, or KoinComponent
    private val onBackClicked: () -> Unit,
) : ShopComponent,
    ComponentContext by componentContext,
    KoinComponent {
    // Inject use cases via Koin (or could be passed via AppGraph if exposed there,
    // but typically we can inject if AppGraph doesn't have them all property listed)
    // Actually AppGraph is preferred if available.
    // For now I'll use Koin inject for the new use cases if they are not in AppGraph interface yet.
    // Wait, I didn't update AppGraph to expose these use cases.
    // It's cleaner to inject them here or update AppGraph.
    // I will use `inject` for expediency as I haven't seen AppGraph definition recently.

    private val buyItemUseCase: BuyItemUseCase by inject()
    private val getPlayerBalanceUseCase: GetPlayerBalanceUseCase by inject()
    private val getShopItemsUseCase: GetShopItemsUseCase by inject()
    private val setActiveCosmeticUseCase: SetActiveCosmeticUseCase by inject()

    // Directly injecting repo for unlocked items for now
    private val playerEconomyRepository: PlayerEconomyRepository by inject()
    private val hapticsService: HapticsService by inject()

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
                val adUnitId = getString(Res.string.ad_unit_id)
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
