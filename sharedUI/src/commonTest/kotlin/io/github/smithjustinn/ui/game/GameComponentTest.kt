package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.domain.usecases.game.*
import io.github.smithjustinn.domain.usecases.stats.GetGameStatsUseCase
import io.github.smithjustinn.domain.usecases.stats.SaveGameResultUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import kotlin.test.*

@OptIn(ExperimentalCoroutinesApi::class)
class GameComponentTest {

    private val logger = Logger(StaticConfig())

    // Repositories (Mocked)
    private val gameStateRepository = mock<GameStateRepository>()
    private val gameStatsRepository = mock<GameStatsRepository>()
    private val leaderboardRepository = mock<LeaderboardRepository>()
    private val settingsRepository = mock<SettingsRepository>()
    private val appGraph = mock<AppGraph>()

    // UseCases (Real instances)
    private val startNewGameUseCase = StartNewGameUseCase()
    private val flipCardUseCase = FlipCardUseCase()
    private val resetErrorCardsUseCase = ResetErrorCardsUseCase()
    private val calculateFinalScoreUseCase = CalculateFinalScoreUseCase()
    private val getGameStatsUseCase = GetGameStatsUseCase(gameStatsRepository)
    private val saveGameResultUseCase = SaveGameResultUseCase(gameStatsRepository, leaderboardRepository, logger)
    private val getSavedGameUseCase = GetSavedGameUseCase(gameStateRepository, logger)
    private val saveGameStateUseCase = SaveGameStateUseCase(gameStateRepository, logger)
    private val clearSavedGameUseCase = ClearSavedGameUseCase(gameStateRepository, logger)

    private val testDispatcher = UnconfinedTestDispatcher()
    private val isPeekEnabledFlow = MutableStateFlow(false)
    private val isWalkthroughCompletedFlow = MutableStateFlow(true)
    private val isMusicEnabledFlow = MutableStateFlow(true)
    private val isSoundEnabledFlow = MutableStateFlow(true)
    private val cardBackThemeFlow = MutableStateFlow(CardBackTheme.GEOMETRIC)
    private val cardSymbolThemeFlow = MutableStateFlow(CardSymbolTheme.CLASSIC)
    private val areSuitsMultiColoredFlow = MutableStateFlow(false)
    private val statsFlow = MutableStateFlow<GameStats?>(null)

    private lateinit var component: DefaultGameComponent
    private var navigatedBack = false

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        isPeekEnabledFlow.value = false
        isWalkthroughCompletedFlow.value = true
        isMusicEnabledFlow.value = true
        isSoundEnabledFlow.value = true
        cardBackThemeFlow.value = CardBackTheme.GEOMETRIC
        cardSymbolThemeFlow.value = CardSymbolTheme.CLASSIC
        areSuitsMultiColoredFlow.value = false
        statsFlow.value = null
        navigatedBack = false
        
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
        every { appGraph.logger } returns logger
        every { appGraph.gameStateRepository } returns gameStateRepository

        every { settingsRepository.isPeekEnabled } returns isPeekEnabledFlow
        every { settingsRepository.isWalkthroughCompleted } returns isWalkthroughCompletedFlow
        every { settingsRepository.isMusicEnabled } returns isMusicEnabledFlow
        every { settingsRepository.isSoundEnabled } returns isSoundEnabledFlow
        every { settingsRepository.cardBackTheme } returns cardBackThemeFlow
        every { settingsRepository.cardSymbolTheme } returns cardSymbolThemeFlow
        every { settingsRepository.areSuitsMultiColored } returns areSuitsMultiColoredFlow
        every { gameStatsRepository.getStatsForDifficulty(any()) } returns statsFlow
        
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStateRepository.saveGameState(any(), any()) } returns Unit
        everySuspend { gameStateRepository.clearSavedGameState() } returns Unit
        everySuspend { settingsRepository.setWalkthroughCompleted(any()) } returns Unit
        
        component = createComponent(8)
    }

    private fun createComponent(
        pairCount: Int, 
        mode: GameMode = GameMode.STANDARD,
        forceNewGame: Boolean = false
    ) = DefaultGameComponent(
        componentContext = DefaultComponentContext(lifecycle = LifecycleRegistry()),
        appGraph = appGraph,
        pairCount = pairCount,
        mode = mode,
        forceNewGame = forceNewGame,
        onBackClicked = { navigatedBack = true }
    )

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // region Initialization & Setup

    @Test
    fun `initial state should have default values`() = runTest {
        component.state.test {
            val state = awaitItem()
            // In init, it calls startGame which updates the state. 
            // Depending on dispatcher, we might see initial or updated. 
            // UnconfinedTestDispatcher usually shows the result of init.
            assertEquals(0, state.elapsedTimeSeconds)
            assertFalse(state.isPeeking)
            assertFalse(state.game.isGameOver)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `StartGame should load stats for the given pair count`() = runTest {
        val pairCount = 6
        val stats = GameStats(pairCount, bestScore = 500, bestTimeSeconds = 45)
        statsFlow.value = stats

        component = createComponent(pairCount)
        
        component.state.test {
            var state = awaitItem()
            while (state.bestScore == 0) {
                state = awaitItem()
            }
            assertEquals(500, state.bestScore)
            assertEquals(45, state.bestTimeSeconds)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `Initialization with forceNewGame should ignore saved game`() = runTest {
        val pairCount = 4
        val savedState = MemoryGameLogic.createInitialState(pairCount).copy(moves = 10)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedState to 100L)

        component = createComponent(pairCount, forceNewGame = true)

        assertEquals(0, component.state.value.game.moves)
        assertEquals(0, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `Initialization should resume existing game if found and matching`() = runTest {
        val pairCount = 4
        val savedState = MemoryGameLogic.createInitialState(pairCount).copy(moves = 5)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedState to 45L)

        component = createComponent(pairCount)

        assertEquals(5, component.state.value.game.moves)
        assertEquals(45, component.state.value.elapsedTimeSeconds)
    }

    // endregion

    // region Walkthrough

    @Test
    fun `Walkthrough should show if not completed`() = runTest {
        isWalkthroughCompletedFlow.value = false
        component = createComponent(8)

        assertTrue(component.state.value.showWalkthrough)
        assertEquals(0, component.state.value.walkthroughStep)
    }

    @Test
    fun `onNextWalkthroughStep should increment step`() = runTest {
        isWalkthroughCompletedFlow.value = false
        component = createComponent(8)

        component.onNextWalkthroughStep()
        assertEquals(1, component.state.value.walkthroughStep)
    }

    @Test
    fun `onCompleteWalkthrough should update repository and hide walkthrough`() = runTest {
        isWalkthroughCompletedFlow.value = false
        component = createComponent(8)

        component.onCompleteWalkthrough()
        
        verifySuspend { settingsRepository.setWalkthroughCompleted(true) }
        assertFalse(component.state.value.showWalkthrough)
    }

    @Test
    fun `FlipCard should be ignored when walkthrough is showing`() = runTest {
        isWalkthroughCompletedFlow.value = false
        component = createComponent(4)
        
        val cardId = component.state.value.game.cards[0].id
        component.onFlipCard(cardId)

        assertFalse(component.state.value.game.cards.first { it.id == cardId }.isFaceUp)
    }

    // endregion

    // region Gameplay Logic

    @Test
    fun `onFlipCard should ignore clicks when peeking`() = runTest {
        val pairCount = 4
        isPeekEnabledFlow.value = true
        isWalkthroughCompletedFlow.value = true
        
        component = createComponent(pairCount)
        assertTrue(component.state.value.isPeeking)

        val cardId = component.state.value.game.cards[0].id
        component.onFlipCard(cardId)

        assertFalse(component.state.value.game.cards.first { it.id == cardId }.isFaceUp)
    }

    @Test
    fun `MatchSuccess should trigger combo explosion for high multipliers`() = runTest {
        val pairCount = 4
        val initialState = MemoryGameLogic.createInitialState(pairCount)
        val card1 = initialState.cards[0]
        val card2 = initialState.cards.first { it.id != card1.id && it.suit == card1.suit && it.rank == card1.rank }
        val card3 = initialState.cards.first { it.id != card1.id && it.id != card2.id }
        val card4 = initialState.cards.first { it.id != card1.id && it.id != card2.id && it.id != card3.id && it.suit == card3.suit && it.rank == card3.rank }

        everySuspend { gameStateRepository.getSavedGameState() } returns (initialState to 0L)
        component = createComponent(pairCount)

        component.onFlipCard(card1.id)
        component.onFlipCard(card2.id)
        component.onFlipCard(card3.id)
        component.onFlipCard(card4.id)

        assertTrue(component.state.value.showComboExplosion)
        
        testDispatcher.scheduler.advanceTimeBy(1001)
        assertFalse(component.state.value.showComboExplosion)
    }

    @Test
    fun `onFlipCard should send PlayFlip event`() = runTest {
        component = createComponent(4)
        val cardId = component.state.value.game.cards[0].id
        
        component.events.test {
            assertEquals(GameUiEvent.PlayDeal, awaitItem())
            component.onFlipCard(cardId)
            assertEquals(GameUiEvent.PlayFlip, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    // endregion

    // region Peeking Logic

    @Test
    fun `Peek should countdown and then start timer`() = runTest {
        isWalkthroughCompletedFlow.value = true
        isPeekEnabledFlow.value = true
        
        component = createComponent(4)
        assertTrue(component.state.value.isPeeking)
        
        testDispatcher.scheduler.advanceTimeBy(3001)
        assertFalse(component.state.value.isPeeking)
        
        testDispatcher.scheduler.advanceTimeBy(1001)
        assertEquals(1, component.state.value.elapsedTimeSeconds)
    }

    // endregion

    // region Time Attack Specifics

    @Test
    fun `Time Attack match should grant time bonus`() = runTest {
        val pairCount = 4
        val mode = GameMode.TIME_ATTACK
        val initialState = MemoryGameLogic.createInitialState(pairCount, mode = mode)
        val card1 = initialState.cards[0]
        val card2 = initialState.cards.first { it.id != card1.id && it.suit == card1.suit && it.rank == card1.rank }

        everySuspend { gameStateRepository.getSavedGameState() } returns (initialState to 30L)
        component = createComponent(pairCount, mode = mode)

        component.onFlipCard(card1.id)
        component.onFlipCard(card2.id)

        val expectedBonus = MemoryGameLogic.calculateTimeGain(1)
        assertTrue(component.state.value.elapsedTimeSeconds > 30L)
        assertEquals(30L + expectedBonus, component.state.value.elapsedTimeSeconds)
        assertTrue(component.state.value.showTimeGain)
        
        testDispatcher.scheduler.advanceTimeBy(1501)
        assertFalse(component.state.value.showTimeGain)
    }

    @Test
    fun `Time Attack mismatch should apply time penalty`() = runTest {
        val pairCount = 4
        val mode = GameMode.TIME_ATTACK
        val initialState = MemoryGameLogic.createInitialState(pairCount, mode = mode)
        val card1 = initialState.cards[0]
        val card2 = initialState.cards.first { it.suit != card1.suit || it.rank != card1.rank }

        everySuspend { gameStateRepository.getSavedGameState() } returns (initialState to 30L)
        component = createComponent(pairCount, mode = mode)

        component.onFlipCard(card1.id)
        component.onFlipCard(card2.id)

        val penalty = MemoryGameLogic.TIME_PENALTY_MISMATCH
        assertEquals(30L - penalty, component.state.value.elapsedTimeSeconds)
        assertTrue(component.state.value.showTimeLoss)
        
        testDispatcher.scheduler.advanceTimeBy(1501)
        assertFalse(component.state.value.showTimeLoss)
    }

    @Test
    fun `Time Attack reaching 0 should trigger game over`() = runTest {
        val pairCount = 4
        val mode = GameMode.TIME_ATTACK
        component = createComponent(pairCount, mode = mode)
        
        // Initial time is 35s for 8 pairs, wait, I used 4. Initial for 4 is unknown in the logic but let's assume it's > 0
        // Actually for 4 pairs it's pairCount * 4 = 16s
        testDispatcher.scheduler.advanceTimeBy(17001)
        
        assertTrue(component.state.value.game.isGameOver)
        verifySuspend { gameStateRepository.clearSavedGameState() }
    }

    // endregion

    // region Game Completion

    @Test
    fun `Game won should calculate final score and check for high score`() = runTest {
        val pairCount = 2
        statsFlow.value = GameStats(pairCount, bestScore = 50, bestTimeSeconds = 0L)
        everySuspend { gameStatsRepository.updateStats(any()) } returns Unit
        everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

        component = createComponent(pairCount)

        // Get the actual cards generated for this game
        val cards = component.state.value.game.cards
        assertTrue(cards.isNotEmpty(), "Game should have cards")
        
        val groups = cards.groupBy { it.suit to it.rank }.values
        
        // Flip each pair
        groups.forEach { pair ->
            pair.forEach { card ->
                component.onFlipCard(card.id)
            }
        }

        val state = component.state.value
        assertTrue(state.game.isGameOver, "Game should be over. Score: ${state.game.score}")
        assertTrue(state.isNewHighScore, "Should be a new high score")
        assertTrue(state.game.score > 50, "Score ${state.game.score} should be > 50")
        
        testDispatcher.scheduler.advanceUntilIdle()
        
        verifySuspend { leaderboardRepository.addEntry(any()) }
        verifySuspend { gameStateRepository.clearSavedGameState() }
    }

    // endregion

    // region Lifecycle & Persistence

    // Decompose lifecycle events need to be triggered manually if using LifecycleRegistry
    @Test
    fun `Destroying component should save the current game state`() = runTest {
        val pairCount = 4
        val lifecycle = LifecycleRegistry()
        component = DefaultGameComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph,
            pairCount = pairCount,
            mode = GameMode.STANDARD,
            forceNewGame = false,
            onBackClicked = {}
        )
        
        val cardId = component.state.value.game.cards[0].id
        component.onFlipCard(cardId)
        
        val currentState = component.state.value
        
        lifecycle.onDestroy()

        verifySuspend { gameStateRepository.saveGameState(currentState.game, currentState.elapsedTimeSeconds) }
    }

    // endregion
}
