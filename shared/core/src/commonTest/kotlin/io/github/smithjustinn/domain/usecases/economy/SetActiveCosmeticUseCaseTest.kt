package io.github.smithjustinn.domain.usecases.economy

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class SetActiveCosmeticUseCaseTest {
    private val repository = mock<PlayerEconomyRepository>()
    private val useCase = SetActiveCosmeticUseCase(repository)

    @Test
    fun `invoke should call selectTheme when itemType is THEME`() =
        runTest {
            everySuspend { repository.isItemUnlocked("theme1") } returns true
            everySuspend { repository.selectTheme("theme1") } returns Unit

            useCase("theme1", ShopItemType.THEME)

            verifySuspend { repository.selectTheme("theme1") }
        }

    @Test
    fun `invoke should call selectSkin when itemType is CARD_SKIN`() =
        runTest {
            everySuspend { repository.isItemUnlocked("skin1") } returns true
            everySuspend { repository.selectSkin("skin1") } returns Unit

            useCase("skin1", ShopItemType.CARD_SKIN)

            verifySuspend { repository.selectSkin("skin1") }
        }

    @Test
    fun `invoke should call selectMusic when itemType is MUSIC`() =
        runTest {
            everySuspend { repository.isItemUnlocked("music1") } returns true
            everySuspend { repository.selectMusic("music1") } returns Unit

            useCase("music1", ShopItemType.MUSIC)

            verifySuspend { repository.selectMusic("music1") }
        }

    @Test
    fun `invoke should call selectPowerUp when itemType is POWER_UP`() =
        runTest {
            everySuspend { repository.isItemUnlocked("powerup1") } returns true
            everySuspend { repository.selectPowerUp("powerup1") } returns Unit

            useCase("powerup1", ShopItemType.POWER_UP)

            verifySuspend { repository.selectPowerUp("powerup1") }
        }
}
