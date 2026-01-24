package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
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
import io.github.smithjustinn.test.runComponentTest
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
        Dispatchers.resetMain()
    }


    private fun createComponent(
        lifecycle: Lifecycle,
        pairCount: Int = 8,
        mode: GameMode = GameMode.STANDARD,
        forceNewGame: Boolean = true
    ): DefaultGameComponent {
        return DefaultGameComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph,
            pairCount = pairCount,
            mode = mode,
            forceNewGame = forceNewGame,
            onBackClicked = {}
        )
    }

    @Test
    fun `starts new game when checking initialization`() = runComponentTest(testDispatcher) { lifecycle ->
        component = createComponent(lifecycle)
        
        component.events.test {
            testDispatcher.scheduler.runCurrent()
            assertEquals(GameUiEvent.PlayDeal, awaitItem())
        }

        val state = component.state.value
        assertEquals(8, state.game.pairCount)
        assertEquals(GameMode.STANDARD, state.game.mode)
        assertFalse(state.isPeeking)
    }

    @Test
    fun `resumes saved game if available and valid`() = runComponentTest(testDispatcher) { lifecycle ->
        val savedGame = MemoryGameState(pairCount = 8, mode = GameMode.STANDARD)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 100L)
        
        component = createComponent(lifecycle, forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        assertEquals(100L, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `onFlipCard updates state and saves game`() = runComponentTest(testDispatcher) { lifecycle ->
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
        
        component = createComponent(lifecycle, forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        component.onFlipCard(1)
        testDispatcher.scheduler.runCurrent()

        assertTrue(component.state.value.game.cards.find { it.id == 1 }?.isFaceUp == true)
        verifySuspend { gameStateRepository.saveGameState(any(), any()) }
    }

    @Test
    fun `timer ticks in Standard mode after initializing`() = runComponentTest(testDispatcher) { lifecycle ->
        component = createComponent(lifecycle, mode = GameMode.STANDARD)
        testDispatcher.scheduler.runCurrent()
        
        assertEquals(0L, component.state.value.elapsedTimeSeconds)
        
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1L, component.state.value.elapsedTimeSeconds)
        
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(2L, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `timer counts down in Time Attack mode`() = runComponentTest(testDispatcher) { lifecycle ->
        component = createComponent(lifecycle, pairCount = 8, mode = GameMode.TIME_ATTACK)
        testDispatcher.scheduler.runCurrent()
        
        val startSeconds = component.state.value.elapsedTimeSeconds
        assertTrue(startSeconds > 0)
        
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(startSeconds - 1, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `peek feature delays timer and shows cards`() = runComponentTest(testDispatcher) { lifecycle ->
        every { settingsRepository.isPeekEnabled } returns MutableStateFlow(true)
        
        component = createComponent(lifecycle, mode = GameMode.STANDARD)
        testDispatcher.scheduler.runCurrent()

        // Should be in peeking state initially
        assertTrue(component.state.value.isPeeking)
        assertEquals(3, component.state.value.peekCountdown)
        assertEquals(0L, component.state.value.elapsedTimeSeconds)

        // Advance time and check countdown
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(2, component.state.value.peekCountdown)

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1, component.state.value.peekCountdown)

        // Peeking ends
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertFalse(component.state.value.isPeeking)

        // Timer should start now
        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(1L, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `matching cards shows combo explosion for high multiplier`() = runComponentTest(testDispatcher) { lifecycle ->
        // Mock a state with high multiplier
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
        val cards = listOf(card1, card2) + (3..16).map { CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two) }
        
        val gameWithCombo = MemoryGameState(
            pairCount = 8,
            mode = GameMode.STANDARD,
            cards = cards,
            comboMultiplier = 3 // High multiplier
        )
        
        everySuspend { gameStateRepository.getSavedGameState() } returns (gameWithCombo to 0L)
        
        component = createComponent(lifecycle, forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        // Flip card 1, then card 2 to trigger match
        component.onFlipCard(1)
        testDispatcher.scheduler.runCurrent()
        
        component.onFlipCard(2)
        testDispatcher.scheduler.runCurrent()
        
        assertTrue(component.state.value.showComboExplosion)
        
        // Should disappear after delay
        testDispatcher.scheduler.advanceTimeBy(1500)
        testDispatcher.scheduler.runCurrent()
        assertFalse(component.state.value.showComboExplosion)
    }

    @Test
    fun `time attack match gains time`() = runComponentTest(testDispatcher) { lifecycle ->
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
        val cards = listOf(card1, card2) + (3..16).map { CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two) }
        
        val savedGame = MemoryGameState(
            pairCount = 8,
            mode = GameMode.TIME_ATTACK,
            cards = cards
        )
        
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 30L)
        
        component = createComponent(lifecycle, forceNewGame = false, mode = GameMode.TIME_ATTACK)
        testDispatcher.scheduler.runCurrent()

        component.onFlipCard(1)
        testDispatcher.scheduler.runCurrent()

        component.onFlipCard(2)
        testDispatcher.scheduler.runCurrent()

        assertTrue(component.state.value.showTimeGain)
        assertTrue(component.state.value.elapsedTimeSeconds > 30) // Gained time
        
        testDispatcher.scheduler.advanceTimeBy(2000)
        testDispatcher.scheduler.runCurrent()
        assertFalse(component.state.value.showTimeGain)
    }
}
