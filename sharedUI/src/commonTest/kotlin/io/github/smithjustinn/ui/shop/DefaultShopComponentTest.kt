package io.github.smithjustinn.ui.shop

import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verify
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import io.github.smithjustinn.domain.usecases.economy.BuyItemUseCase
import io.github.smithjustinn.domain.usecases.economy.GetPlayerBalanceUseCase
import io.github.smithjustinn.domain.usecases.economy.GetShopItemsUseCase
import io.github.smithjustinn.domain.usecases.economy.SetActiveCosmeticUseCase
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class DefaultShopComponentTest {
    private val playerEconomyRepository = mock<PlayerEconomyRepository>()
    private val shopItemRepository = mock<ShopItemRepository>()
    private val hapticsService = mock<HapticsService>()
    private val appGraph = mock<AppGraph>()

    // Use real use cases since they are final classes and cannot be mocked by Mokkery
    private val buyItemUseCase = BuyItemUseCase(playerEconomyRepository, shopItemRepository)
    private val getPlayerBalanceUseCase = GetPlayerBalanceUseCase(playerEconomyRepository)
    private val getShopItemsUseCase = GetShopItemsUseCase(shopItemRepository)
    private val setActiveCosmeticUseCase = SetActiveCosmeticUseCase(playerEconomyRepository)

    private val testDispatcher = StandardTestDispatcher()
    private val coroutineDispatchers =
        CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )

    private val lifecycle = LifecycleRegistry()
    private val componentContext = DefaultComponentContext(lifecycle)

    @BeforeTest
    fun setup() {
        every { appGraph.coroutineDispatchers } returns coroutineDispatchers
        every { playerEconomyRepository.balance } returns MutableStateFlow(1000L)
        every { playerEconomyRepository.unlockedItemIds } returns MutableStateFlow(emptySet())
        every { playerEconomyRepository.selectedTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { playerEconomyRepository.selectedThemeId } returns MutableStateFlow(CardBackTheme.GEOMETRIC.id)
        every { playerEconomyRepository.selectedSkin } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { playerEconomyRepository.selectedSkinId } returns MutableStateFlow(CardSymbolTheme.CLASSIC.id)
        everySuspend { shopItemRepository.getShopItems() } returns emptyList()

        startKoin {
            modules(
                module {
                    single { buyItemUseCase }
                    single { getPlayerBalanceUseCase }
                    single { getShopItemsUseCase }
                    single { setActiveCosmeticUseCase }
                    single { playerEconomyRepository }
                    single { hapticsService }
                    single { shopItemRepository }
                },
            )
        }
    }

    @AfterTest
    fun tearDown() {
        stopKoin()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onBuyItemClicked should trigger use case and haptics on success`() =
        runTest(testDispatcher) {
            val item =
                ShopItem(
                    id = "item1",
                    name = "Item 1",
                    price = 100L,
                    type = ShopItemType.CARD_SKIN,
                    description = "",
                )

            everySuspend { shopItemRepository.getShopItems() } returns listOf(item)
            everySuspend { playerEconomyRepository.isItemUnlocked("item1") } returns false
            everySuspend { playerEconomyRepository.deductCurrency(100L) } returns true
            everySuspend { playerEconomyRepository.unlockItem("item1") } returns Unit
            every { hapticsService.performHapticFeedback(HapticFeedbackType.LONG_PRESS) } returns Unit

            val component =
                DefaultShopComponent(
                    componentContext = componentContext,
                    appGraph = appGraph,
                    onBackClicked = {},
                )

            component.onBuyItemClicked(item)
            advanceUntilIdle()

            verifySuspend { playerEconomyRepository.deductCurrency(100L) }
            verifySuspend { playerEconomyRepository.unlockItem("item1") }
            verify { hapticsService.performHapticFeedback(HapticFeedbackType.LONG_PRESS) }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `onBuyItemClicked should not trigger haptics on failure`() =
        runTest(testDispatcher) {
            val item =
                ShopItem(
                    id = "item1",
                    name = "Item 1",
                    price = 100L,
                    type = ShopItemType.CARD_SKIN,
                    description = "",
                )

            everySuspend { shopItemRepository.getShopItems() } returns listOf(item)
            everySuspend { playerEconomyRepository.isItemUnlocked("item1") } returns false
            everySuspend { playerEconomyRepository.deductCurrency(100L) } returns false

            val component =
                DefaultShopComponent(
                    componentContext = componentContext,
                    appGraph = appGraph,
                    onBackClicked = {},
                )

            component.onBuyItemClicked(item)
            advanceUntilIdle()

            verifySuspend { playerEconomyRepository.deductCurrency(100L) }
            verify(
                VerifyMode
                    .exactly(0),
            ) {
                hapticsService.performHapticFeedback(HapticFeedbackType.LONG_PRESS)
            }
        }
}
