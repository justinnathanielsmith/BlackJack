package io.github.smithjustinn.ui.root

import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.CalculateFinalScoreUseCase
import io.github.smithjustinn.domain.usecases.game.ClearSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.FlipCardUseCase
import io.github.smithjustinn.domain.usecases.game.GetSavedGameUseCase
import io.github.smithjustinn.domain.usecases.game.ResetErrorCardsUseCase
import io.github.smithjustinn.domain.usecases.game.SaveGameStateUseCase
import io.github.smithjustinn.domain.usecases.game.StartNewGameUseCase
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootComponentTest {

    private val gameStateRepository: GameStateRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val gameStatsRepository: GameStatsRepository = mock()
    private val leaderboardRepository: LeaderboardRepository = mock()
    private val logger: Logger = Logger(StaticConfig())

    private val appGraph: AppGraph = mock()
    private val testDispatcher = StandardTestDispatcher()
    private lateinit var lifecycle: LifecycleRegistry

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { appGraph.gameStateRepository } returns gameStateRepository
        every { appGraph.settingsRepository } returns settingsRepository
        every { appGraph.gameStatsRepository } returns gameStatsRepository
        every { appGraph.leaderboardRepository } returns leaderboardRepository
        every { appGraph.logger } returns logger

        // Mock use cases if needed (RootComponent creates children which use them)
        every { appGraph.startNewGameUseCase } returns StartNewGameUseCase()
        every { appGraph.flipCardUseCase } returns FlipCardUseCase()
        every { appGraph.resetErrorCardsUseCase } returns ResetErrorCardsUseCase()
        every { appGraph.calculateFinalScoreUseCase } returns CalculateFinalScoreUseCase()
        every { appGraph.getGameStatsUseCase } returns GetGameStatsUseCase(gameStatsRepository)
        every { appGraph.saveGameResultUseCase } returns SaveGameResultUseCase(gameStatsRepository, leaderboardRepository, logger)
        every { appGraph.getSavedGameUseCase } returns GetSavedGameUseCase(gameStateRepository, logger)
        every { appGraph.saveGameStateUseCase } returns SaveGameStateUseCase(gameStateRepository, logger)
        every { appGraph.clearSavedGameUseCase } returns ClearSavedGameUseCase(gameStateRepository, logger)

        every { appGraph.coroutineDispatchers } returns CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )

        every { settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { settingsRepository.cardSymbolTheme } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { settingsRepository.isPeekEnabled } returns MutableStateFlow(false)
        every { settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(true)
        every { settingsRepository.isMusicEnabled } returns MutableStateFlow(true)
        every { settingsRepository.isSoundEnabled } returns MutableStateFlow(true)
        every { settingsRepository.areSuitsMultiColored } returns MutableStateFlow(false)
        every { settingsRepository.soundVolume } returns MutableStateFlow(0.8f)
        every { settingsRepository.musicVolume } returns MutableStateFlow(0.5f)
        
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStatsRepository.getStatsForDifficulty(any()) } returns MutableStateFlow(null)
        every { leaderboardRepository.getTopEntries(any(), any()) } returns MutableStateFlow(emptyList())

        lifecycle = LifecycleRegistry()
        lifecycle.onCreate()
    }

    @AfterTest
    fun tearDown() {
        lifecycle.onDestroy()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial child is Start`() {
        val root = DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph
        )

        assertTrue(root.childStack.value.active.instance is RootComponent.Child.Start)
    }

    @Test
    fun `navigating to Game updates stack`() {
        val root = DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph
        )

        val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
        startChild.component.onStartGame()

        assertTrue(root.childStack.value.active.instance is RootComponent.Child.Game)
    }

    @Test
    fun `navigating to Settings updates stack`() {
        val root = DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph
        )

        val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
        startChild.component.onSettingsClick()

        assertTrue(root.childStack.value.active.instance is RootComponent.Child.Settings)
    }

    @Test
    fun `navigating to Stats updates stack`() {
        val root = DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph
        )

        val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
        startChild.component.onStatsClick()

        assertTrue(root.childStack.value.active.instance is RootComponent.Child.Stats)
    }
}
