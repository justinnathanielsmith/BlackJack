package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import dev.mokkery.matcher.any
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.*
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.*
import io.github.smithjustinn.domain.usecases.stats.*
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.time.Duration.Companion.seconds
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameComponentTest {

    private val gameStateRepository: GameStateRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val gameStatsRepository: GameStatsRepository = mock()
    private val leaderboardRepository: LeaderboardRepository = mock()
    private val logger: Logger = Logger(StaticConfig())

    private val startNewGameUseCase = StartNewGameUseCase()
    private val flipCardUseCase = FlipCardUseCase()
    private val resetErrorCardsUseCase = ResetErrorCardsUseCase()
    private val calculateFinalScoreUseCase = CalculateFinalScoreUseCase()
    private val getGameStatsUseCase = GetGameStatsUseCase(gameStatsRepository)
    private val saveGameResultUseCase = SaveGameResultUseCase(gameStatsRepository, leaderboardRepository, logger)
    private val getSavedGameUseCase = GetSavedGameUseCase(gameStateRepository, logger)
    private val saveGameStateUseCase = SaveGameStateUseCase(gameStateRepository, logger)
    private val clearSavedGameUseCase = ClearSavedGameUseCase(gameStateRepository, logger)
    
    private val appGraph: AppGraph = mock()

    private lateinit var component: DefaultGameComponent
    private val testDispatcher = StandardTestDispatcher()
    private var lifecycle: LifecycleRegistry? = null

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        every { appGraph.startNewGameUseCase } returns startNewGameUseCase
        every { appGraph.flipCardUseCase } returns flipCardUseCase
        every { appGraph.resetErrorCardsUseCase } returns resetErrorCardsUseCase
        every { appGraph.calculateFinalScoreUseCase } returns calculateFinalScoreUseCase
        every { appGraph.getGameStatsUseCase } returns getGameStatsUseCase
        every { appGraph.saveGameResultUseCase } returns saveGameResultUseCase
        every { appGraph.getSavedGameUseCase } returns getSavedGameUseCase
        every { appGraph.saveGameStateUseCase } returns saveGameStateUseCase
        every { appGraph.clearSavedGameUseCase } returns clearSavedGameUseCase
        every { appGraph.settingsRepository } returns settingsRepository
        every { appGraph.leaderboardRepository } returns leaderboardRepository
        every { appGraph.gameStateRepository } returns gameStateRepository
        every { appGraph.gameStatsRepository } returns gameStatsRepository
        every { appGraph.logger } returns logger
        
        every { appGraph.coroutineDispatchers } returns CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )

        // Default mock behaviors
        every { settingsRepository.isPeekEnabled } returns MutableStateFlow(false)
        every { settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(true)
        every { settingsRepository.isMusicEnabled } returns MutableStateFlow(true)
        every { settingsRepository.isSoundEnabled } returns MutableStateFlow(true)
        every { settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { settingsRepository.cardSymbolTheme } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { settingsRepository.areSuitsMultiColored } returns MutableStateFlow(false)

        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStateRepository.saveGameState(any(), any()) } returns Unit
        everySuspend { gameStatsRepository.getStatsForDifficulty(any()) } returns MutableStateFlow(null)
    }

    @AfterTest
    fun tearDown() {
        lifecycle?.onDestroy()
        Dispatchers.resetMain()
    }


    private fun runGameTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher, timeout = 10.seconds) {
        val l = LifecycleRegistry()
        l.onCreate()
        lifecycle = l
        try {
            block()
        } finally {
            l.onDestroy()
            lifecycle = null
        }
    }

    private fun createComponent(
        pairCount: Int = 8,
        mode: GameMode = GameMode.STANDARD,
        forceNewGame: Boolean = true
    ): DefaultGameComponent {
        return DefaultGameComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle!!),
            appGraph = appGraph,
            pairCount = pairCount,
            mode = mode,
            forceNewGame = forceNewGame,
            onBackClicked = {}
        )
    }

    @Test
    fun `starts new game when checking initialization`() = runGameTest {
        component = createComponent()
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            val state = awaitItem()
            // Real logic creates shuffled cards, so we just check structural properties
            assertEquals(8, state.game.pairCount)
            assertEquals(GameMode.STANDARD, state.game.mode)
            assertFalse(state.isPeeking)
        }
    }

    @Test
    fun `resumes saved game if available and valid`() = runGameTest {
        val savedGame = MemoryGameState(pairCount = 8, mode = GameMode.STANDARD)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 100L)
        
        component = createComponent(forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            val state = awaitItem()
            // We check if game was resumed by checking if elapsedTime matches
            // However, startNewGameUseCase also runs if we don't find saved game.
            // If we found saved game, we update state.
            assertEquals(100L, state.elapsedTimeSeconds)
        }
    }

    @Test
    fun `onFlipCard updates state and saves game`() = runGameTest {
        // Setup a deterministic saved game state
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
        val otherCards = (3..16).map { 
             CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two) 
        }
        val savedGame = MemoryGameState(
            pairCount = 8, 
            mode = GameMode.STANDARD, 
            cards = listOf(card1, card2) + otherCards
        )
        
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 0L)
        
        // Resume game to use our known state
        component = createComponent(forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        // Flip first card
        component.onFlipCard(1)
        testDispatcher.scheduler.runCurrent()

        // Verify state update (card flipped)
        assertTrue(component.state.value.game.cards.find { it.id == 1 }?.isFaceUp == true)
        
        // Verify saving happened
        verifySuspend { gameStateRepository.saveGameState(any(), any()) }
    }

    @Test
    fun `timer ticks in Standard mode`() = runGameTest {
        component = createComponent(mode = GameMode.STANDARD)
        testDispatcher.scheduler.runCurrent() // Run initialization
        
        component.state.test {
            // Updated after init
            val initial = awaitItem()
            assertEquals(0L, initial.elapsedTimeSeconds)
            
            testDispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(1L, awaitItem().elapsedTimeSeconds)
            
            testDispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(2L, awaitItem().elapsedTimeSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `timer counts down in Time Attack mode`() = runGameTest {
        component = createComponent(pairCount = 8, mode = GameMode.TIME_ATTACK)
        testDispatcher.scheduler.runCurrent() // Run initialization
        
        component.state.test {
            val initial = awaitItem()
            val startSeconds = initial.elapsedTimeSeconds
            assertTrue(startSeconds > 0)
            
            testDispatcher.scheduler.advanceTimeBy(1000)
            assertEquals(startSeconds - 1, awaitItem().elapsedTimeSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
