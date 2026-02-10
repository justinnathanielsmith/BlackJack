package io.github.smithjustinn.domain.usecases.economy

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verify.VerifyMode
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.ShopItem
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BuyItemUseCaseTest {
    private val repository = mock<PlayerEconomyRepository>()
    private val shopItemRepository = mock<ShopItemRepository>()
    private val useCase = BuyItemUseCase(repository, shopItemRepository)

    private val testItem =
        ShopItem(
            id = "item1",
            name = "Test Item",
            description = "Description",
            price = 100,
            type = ShopItemType.THEME,
        )

    @Test
    fun `invoke should return success if item is already unlocked`() =
        runTest {
            everySuspend { repository.isItemUnlocked("item1") } returns true

            val result = useCase("item1")

            assertTrue(result.isSuccess)
            verifySuspend(VerifyMode.exactly(0)) { repository.deductCurrency(any()) }
        }

    @Test
    fun `invoke should deduct currency and unlock item if sufficient funds`() =
        runTest {
            everySuspend { repository.isItemUnlocked("item1") } returns false
            everySuspend { shopItemRepository.getShopItems() } returns listOf(testItem)
            everySuspend { repository.deductCurrency(100) } returns true
            everySuspend { repository.unlockItem("item1") } returns Unit

            val result = useCase("item1")

            assertTrue(result.isSuccess)
            verifySuspend { repository.deductCurrency(100) }
            verifySuspend { repository.unlockItem("item1") }
        }

    @Test
    fun `invoke should return failure if insufficient funds`() =
        runTest {
            everySuspend { repository.isItemUnlocked("item1") } returns false
            everySuspend { shopItemRepository.getShopItems() } returns listOf(testItem)
            everySuspend { repository.deductCurrency(100) } returns false

            val result = useCase("item1")

            assertTrue(result.isFailure)
            verifySuspend { repository.deductCurrency(100) }
            verifySuspend(VerifyMode.exactly(0)) { repository.unlockItem(any()) }
        }

    @Test
    fun `invoke should return failure if item not found`() =
        runTest {
            everySuspend { repository.isItemUnlocked("unknown") } returns false
            everySuspend { shopItemRepository.getShopItems() } returns listOf(testItem)

            val result = useCase("unknown")

            assertTrue(result.isFailure)
            verifySuspend(VerifyMode.exactly(0)) { repository.deductCurrency(any()) }
        }
}
