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
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.*
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher

/**
 * A centralized context for Mokkery-based tests. Provides pre-configured mocks for repositories and
 * services, and an [AppGraph] that returns them.
 */
class MokkeryTestContext(testDispatcher: TestDispatcher) {
    val gameStateRepository: GameStateRepository = mock()
    val settingsRepository: SettingsRepository = mock()
    val gameStatsRepository: GameStatsRepository = mock()
    val leaderboardRepository: LeaderboardRepository = mock()
    val audioService: AudioService = mock()
    val hapticsService: HapticsService = mock()
    val logger: Logger = Logger(StaticConfig())

    val appGraph: AppGraph = mock()

    val coroutineDispatchers =
            CoroutineDispatchers(
                    main = testDispatcher,
                    mainImmediate = testDispatcher,
                    io = testDispatcher,
                    default = testDispatcher
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
        every { appGraph.audioService } returns audioService
        every { appGraph.hapticsService } returns hapticsService
        every { appGraph.logger } returns logger
        every { appGraph.coroutineDispatchers } returns coroutineDispatchers

        // UseCases - initialized with mocked repositories where needed
        every { appGraph.startNewGameUseCase } returns StartNewGameUseCase()
        every { appGraph.flipCardUseCase } returns FlipCardUseCase()
        every { appGraph.resetErrorCardsUseCase } returns ResetErrorCardsUseCase()
        every { appGraph.calculateFinalScoreUseCase } returns CalculateFinalScoreUseCase()
        every { appGraph.getGameStatsUseCase } returns GetGameStatsUseCase(gameStatsRepository)
        every { appGraph.saveGameResultUseCase } returns
                SaveGameResultUseCase(gameStatsRepository, leaderboardRepository, logger)
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
        every { settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { settingsRepository.cardSymbolTheme } returns
                MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { settingsRepository.areSuitsMultiColored } returns MutableStateFlow(false)

        // Repository defaults
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStateRepository.saveGameState(any(), any()) } returns Unit
        everySuspend { gameStatsRepository.getStatsForDifficulty(any()) } returns
                MutableStateFlow(null)

        // Leaderboard defaults
        DifficultyLevel.defaultLevels.forEach { level ->
            every { leaderboardRepository.getTopEntries(level.pairs, any()) } returns
                    MutableStateFlow(emptyList())
        }
    }
}
