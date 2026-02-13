package io.github.smithjustinn.domain.usecases.economy

import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.GameMusic
import io.github.smithjustinn.domain.models.ShopItemType
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertFailsWith

class SetActiveCosmeticUseCaseSecurityTest {

    private val repository = mock<PlayerEconomyRepository>()
    private val useCase = SetActiveCosmeticUseCase(repository)

    @Test
    fun `should prevent equipping locked items`() = runTest {
        // Given an item is NOT unlocked
        val lockedItemId = "theme_expensive"
        everySuspend { repository.isItemUnlocked(lockedItemId) } returns false

        // When we try to equip it, it should fail
        assertFailsWith<IllegalArgumentException> {
            useCase(lockedItemId, ShopItemType.THEME)
        }

        // And the repository select method should NOT be called (implied by crash before call)
    }

    @Test
    fun `should allow equipping unlocked items`() = runTest {
        val unlockedItemId = "theme_owned"
        everySuspend { repository.isItemUnlocked(unlockedItemId) } returns true
        everySuspend { repository.selectTheme(unlockedItemId) } returns Unit

        useCase(unlockedItemId, ShopItemType.THEME)

        verifySuspend { repository.selectTheme(unlockedItemId) }
    }

    @Test
    fun `should allow equipping default items even if not explicitly unlocked`() = runTest {
        val defaultMusicId = GameMusic.DEFAULT.id
        // Setup: mocking selectMusic is required
        everySuspend { repository.selectMusic(defaultMusicId) } returns Unit

        // Note: isItemUnlocked should NOT be called for defaults due to short-circuiting

        useCase(defaultMusicId, ShopItemType.MUSIC)

        verifySuspend { repository.selectMusic(defaultMusicId) }
    }
}
