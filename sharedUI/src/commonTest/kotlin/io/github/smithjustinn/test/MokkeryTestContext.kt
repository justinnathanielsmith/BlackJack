package io.github.smithjustinn.test

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.repositories.DailyChallengeRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.PlayerEconomyRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.repositories.ShopItemRepository
import io.github.smithjustinn.domain.services.AdService
import io.github.smithjustinn.domain.usecases.economy.DefaultEarnCurrencyUseCase
import io.github.smithjustinn.domain.usecases.game.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.game.ClearSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.game.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.game.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.game.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.TestScope

/**
 * A centralized context for Mokkery-based tests. Provides pre-configured mocks for repositories and
 * services, and an [AppGraph] that returns them.
 */
class MokkeryTestContext(
    testDispatcher: TestDispatcher,
) {
    val gameStateRepository: GameStateRepository = mock()
    val settingsRepository: SettingsRepository = mock()
    val gameStatsRepository: GameStatsRepository = mock()
    val leaderboardRepository: LeaderboardRepository = mock()
    val dailyChallengeRepository: DailyChallengeRepository = mock()
    val playerEconomyRepository: PlayerEconomyRepository = mock()
    val shopItemRepository: ShopItemRepository = mock()
    val audioService: AudioService = mock()
    val hapticsService: HapticsService = mock()
    val adService: AdService = mock()
    val logger: Logger = Logger(StaticConfig())

    val appGraph: AppGraph = mock()

    val coroutineDispatchers =
        CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher,
        )

    init {
        setupAppGraph()
        setupDefaultMockBehaviors()
    }

    private fun setupAppGraph() {
        every { appGraph.gameStateRepository } returns gameStateRepository
        every { appGraph.settingsRepository } returns settingsRepository
        every { appGraph.gameStatsRepository } returns gameStatsRepository
        every { appGraph.leaderboardRepository } returns leaderboardRepository
        every { appGraph.dailyChallengeRepository } returns dailyChallengeRepository
        every { appGraph.playerEconomyRepository } returns playerEconomyRepository
        every { appGraph.shopItemRepository } returns shopItemRepository
        every { appGraph.audioService } returns audioService
        every { appGraph.hapticsService } returns hapticsService
        every { appGraph.adService } returns adService
        every { appGraph.logger } returns logger
        every { appGraph.coroutineDispatchers } returns coroutineDispatchers
        every { appGraph.applicationScope } returns TestScope(coroutineDispatchers.main)

        // UseCases - initialized with mocked repositories where needed

        // UseCases - initialized with mocked repositories where needed
        every { appGraph.startNewGameUseCase } returns StartNewGameUseCase()
        every { appGraph.flipCardUseCase } returns FlipCardUseCase()
        every { appGraph.resetErrorCardsUseCase } returns ResetErrorCardsUseCase()
        every { appGraph.calculateFinalScoreUseCase } returns CalculateFinalScoreUseCase()
        every { appGraph.getGameStatsUseCase } returns GetGameStatsUseCase(gameStatsRepository)
        every { appGraph.saveGameResultUseCase } returns
            SaveGameResultUseCase(gameStatsRepository, leaderboardRepository, logger)
        every { appGraph.earnCurrencyUseCase } returns DefaultEarnCurrencyUseCase(playerEconomyRepository)
        every { appGraph.getSavedGameUseCase } returns
            GetSavedGameUseCase(gameStateRepository, logger)
        every { appGraph.saveGameStateUseCase } returns
            SaveGameStateUseCase(gameStateRepository, logger)
        every { appGraph.clearSavedGameUseCase } returns
            ClearSavedGameUseCase(gameStateRepository, logger)
    }

    private fun setupDefaultMockBehaviors() {
        // Settings defaults
        every { settingsRepository.isPeekEnabled } returns MutableStateFlow(false)
        every { settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(true)
        every { settingsRepository.isMusicEnabled } returns MutableStateFlow(true)
        every { settingsRepository.isSoundEnabled } returns MutableStateFlow(true)
        every { settingsRepository.soundVolume } returns MutableStateFlow(0.8f)
        every { settingsRepository.musicVolume } returns MutableStateFlow(0.5f)
        every { settingsRepository.areSuitsMultiColored } returns MutableStateFlow(false)

        every { playerEconomyRepository.selectedTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { playerEconomyRepository.selectedThemeId } returns MutableStateFlow("GEOMETRIC")
        every { playerEconomyRepository.selectedSkin } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { playerEconomyRepository.balance } returns MutableStateFlow(1000L)
        every { playerEconomyRepository.unlockedItemIds } returns MutableStateFlow(emptySet())
        everySuspend { shopItemRepository.getShopItems() } returns emptyList()

        // Repository defaults
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStateRepository.saveGameState(any(), any()) } returns Unit
        everySuspend { gameStatsRepository.getStatsForDifficulty(any()) } returns
            MutableStateFlow(null)
        every { gameStatsRepository.getAllStats() } returns MutableStateFlow(emptyList())

        everySuspend { adService.loadRewardedAd(any()) } returns Unit
        everySuspend { adService.showRewardedAd(any()) } returns Unit

        // Leaderboard defaults
        DifficultyLevel.defaultLevels.forEach { level ->
            every { leaderboardRepository.getTopEntries(level.pairs, any()) } returns
                MutableStateFlow(emptyList())
        }

        everySuspend { dailyChallengeRepository.isChallengeCompleted(any()) } returns MutableStateFlow(false)
    }
}
