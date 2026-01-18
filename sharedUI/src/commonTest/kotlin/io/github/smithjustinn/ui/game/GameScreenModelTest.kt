package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.MemoryGameLogic
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
class GameScreenModelTest {

    private val logger = Logger(StaticConfig())

    // Repositories (Mocked)
    private val gameStateRepository = mock<GameStateRepository>()
    private val gameStatsRepository = mock<GameStatsRepository>()
    private val leaderboardRepository = mock<LeaderboardRepository>()
    private val settingsRepository = mock<SettingsRepository>()

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
    private val statsFlow = MutableStateFlow<GameStats?>(null)

    private lateinit var screenModel: GameScreenModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        isPeekEnabledFlow.value = false
        isWalkthroughCompletedFlow.value = true
        statsFlow.value = null
        
        every { settingsRepository.isPeekEnabled } returns isPeekEnabledFlow
        every { settingsRepository.isWalkthroughCompleted } returns isWalkthroughCompletedFlow
        every { gameStatsRepository.getStatsForDifficulty(any()) } returns statsFlow
        
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStateRepository.saveGameState(any(), any()) } returns Unit
        everySuspend { gameStateRepository.clearSavedGameState() } returns Unit
        everySuspend { settingsRepository.setWalkthroughCompleted(any()) } returns Unit
        
        screenModel = createScreenModel()
    }

    private fun createScreenModel() = GameScreenModel(
        startNewGameUseCase,
        flipCardUseCase,
        resetErrorCardsUseCase,
        calculateFinalScoreUseCase,
        getGameStatsUseCase,
        saveGameResultUseCase,
        getSavedGameUseCase,
        saveGameStateUseCase,
        clearSavedGameUseCase,
        settingsRepository,
        logger
    )

    @AfterTest
    fun tearDown() {
        if (::screenModel.isInitialized) {
            screenModel.onDispose()
        }
        Dispatchers.resetMain()
    }

    // region Initialization & Setup

    @Test
    fun `initial state should have default values`() = runTest {
        screenModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.elapsedTimeSeconds)
            assertFalse(state.isPeeking)
            assertFalse(state.game.isGameOver)
            cancelAndIgnoreRemainingEvents()
        }
        screenModel.onDispose()
    }

    @Test
    fun `StartGame should load stats for the given pair count`() = runTest {
        val pairCount = 6
        val stats = GameStats(pairCount, bestScore = 500, bestTimeSeconds = 45)
        statsFlow.value = stats

        screenModel.handleIntent(GameIntent.StartGame(pairCount))
        
        screenModel.state.test {
            var state = awaitItem()
            while (state.bestScore == 0) {
                state = awaitItem()
            }
            assertEquals(500, state.bestScore)
            assertEquals(45, state.bestTimeSeconds)
            cancelAndIgnoreRemainingEvents()
        }
        screenModel.onDispose()
    }

    @Test
    fun `StartGame with forceNewGame should ignore saved game`() = runTest {
        val pairCount = 4
        val savedState = MemoryGameLogic.createInitialState(pairCount).copy(moves = 10)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedState to 100L)

        screenModel.handleIntent(GameIntent.StartGame(pairCount, forceNewGame = true))

        assertEquals(0, screenModel.state.value.game.moves)
        assertEquals(0, screenModel.state.value.elapsedTimeSeconds)
        screenModel.onDispose()
    }

    @Test
    fun `StartGame should resume existing game if found and matching`() = runTest {
        val pairCount = 4
        val savedState = MemoryGameLogic.createInitialState(pairCount).copy(moves = 5)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedState to 45L)

        screenModel.handleIntent(GameIntent.StartGame(pairCount))

        assertEquals(5, screenModel.state.value.game.moves)
        assertEquals(45, screenModel.state.value.elapsedTimeSeconds)
        screenModel.onDispose()
    }

    // endregion

    // region Walkthrough

    @Test
    fun `Walkthrough should show if not completed`() = runTest {
        screenModel.onDispose()
        isWalkthroughCompletedFlow.value = false
        screenModel = createScreenModel()

        assertTrue(screenModel.state.value.showWalkthrough)
        assertEquals(0, screenModel.state.value.walkthroughStep)
        screenModel.onDispose()
    }

    @Test
    fun `NextWalkthroughStep should increment step`() = runTest {
        screenModel.onDispose()
        isWalkthroughCompletedFlow.value = false
        screenModel = createScreenModel()

        screenModel.handleIntent(GameIntent.NextWalkthroughStep)
        assertEquals(1, screenModel.state.value.walkthroughStep)
        screenModel.onDispose()
    }

    @Test
    fun `CompleteWalkthrough should update repository and hide walkthrough`() = runTest {
        screenModel.onDispose()
        isWalkthroughCompletedFlow.value = false
        screenModel = createScreenModel()

        screenModel.handleIntent(GameIntent.CompleteWalkthrough)
        
        verifySuspend { settingsRepository.setWalkthroughCompleted(true) }
        assertFalse(screenModel.state.value.showWalkthrough)
        screenModel.onDispose()
    }

    @Test
    fun `CompleteWalkthrough should start peek if enabled`() = runTest {
        screenModel.onDispose()
        isWalkthroughCompletedFlow.value = false
        isPeekEnabledFlow.value = true
        screenModel = createScreenModel()

        screenModel.handleIntent(GameIntent.CompleteWalkthrough)
        
        assertTrue(screenModel.state.value.isPeeking)
        assertEquals(3, screenModel.state.value.peekCountdown)
        screenModel.onDispose()
    }

    @Test
    fun `FlipCard should be ignored when walkthrough is showing`() = runTest {
        screenModel.onDispose()
        isWalkthroughCompletedFlow.value = false
        screenModel = createScreenModel()
        
        screenModel.handleIntent(GameIntent.StartGame(4))
        val cardId = screenModel.state.value.game.cards[0].id
        screenModel.handleIntent(GameIntent.FlipCard(cardId))

        assertFalse(screenModel.state.value.game.cards.first { it.id == cardId }.isFaceUp)
        screenModel.onDispose()
    }

    // endregion

    // region Gameplay Logic

    @Test
    fun `FlipCard should ignore clicks when peeking`() = runTest {
        val pairCount = 4
        isPeekEnabledFlow.value = true
        
        screenModel.handleIntent(GameIntent.StartGame(pairCount))
        assertTrue(screenModel.state.value.isPeeking)

        val cardId = screenModel.state.value.game.cards[0].id
        screenModel.handleIntent(GameIntent.FlipCard(cardId))

        assertFalse(screenModel.state.value.game.cards.first { it.id == cardId }.isFaceUp)
        screenModel.onDispose()
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
        screenModel.handleIntent(GameIntent.StartGame(pairCount))

        screenModel.handleIntent(GameIntent.FlipCard(card1.id))
        screenModel.handleIntent(GameIntent.FlipCard(card2.id))
        screenModel.handleIntent(GameIntent.FlipCard(card3.id))
        screenModel.handleIntent(GameIntent.FlipCard(card4.id))

        assertTrue(screenModel.state.value.showComboExplosion)
        
        testDispatcher.scheduler.advanceTimeBy(1001)
        assertFalse(screenModel.state.value.showComboExplosion)
        screenModel.onDispose()
    }

    @Test
    fun `Resuming a game with error cards should trigger reset`() = runTest {
        val pairCount = 4
        var savedState = MemoryGameLogic.createInitialState(pairCount)
        val card1 = savedState.cards[0].copy(isFaceUp = true, isError = true)
        val card2 = savedState.cards[1].copy(isFaceUp = true, isError = true)
        savedState = savedState.copy(cards = savedState.cards.map { 
            when (it.id) {
                card1.id -> card1
                card2.id -> card2
                else -> it
            }
        })
        
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedState to 10L)

        screenModel.handleIntent(GameIntent.StartGame(pairCount))

        assertTrue(screenModel.state.value.game.cards.first { it.id == card1.id }.isFaceUp)
        
        testDispatcher.scheduler.advanceTimeBy(501)
        assertFalse(screenModel.state.value.game.cards.first { it.id == card1.id }.isFaceUp)
        screenModel.onDispose()
    }

    @Test
    fun `FlipCard should send PlayFlip event`() = runTest {
        screenModel.handleIntent(GameIntent.StartGame(4))
        val cardId = screenModel.state.value.game.cards[0].id
        
        screenModel.events.test {
            assertEquals(GameUiEvent.PlayDeal, awaitItem())
            screenModel.handleIntent(GameIntent.FlipCard(cardId))
            assertEquals(GameUiEvent.PlayFlip, awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
        screenModel.onDispose()
    }

    // endregion

    // region Peeking Logic

    @Test
    fun `Peek should countdown and then start timer`() = runTest {
        screenModel.onDispose()
        isWalkthroughCompletedFlow.value = true
        isPeekEnabledFlow.value = true
        screenModel = createScreenModel()

        screenModel.handleIntent(GameIntent.StartGame(4))
        assertTrue(screenModel.state.value.isPeeking)
        
        testDispatcher.scheduler.advanceTimeBy(3001)
        assertFalse(screenModel.state.value.isPeeking)
        
        testDispatcher.scheduler.advanceTimeBy(1001)
        assertEquals(1, screenModel.state.value.elapsedTimeSeconds)
        screenModel.onDispose()
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
        screenModel.handleIntent(GameIntent.StartGame(pairCount, mode = mode))

        screenModel.handleIntent(GameIntent.FlipCard(card1.id))
        screenModel.handleIntent(GameIntent.FlipCard(card2.id))

        val expectedBonus = MemoryGameLogic.calculateTimeGain(1)
        assertTrue(screenModel.state.value.elapsedTimeSeconds > 30L)
        assertEquals(30L + expectedBonus, screenModel.state.value.elapsedTimeSeconds)
        assertTrue(screenModel.state.value.showTimeGain)
        
        testDispatcher.scheduler.advanceTimeBy(1501)
        assertFalse(screenModel.state.value.showTimeGain)
        screenModel.onDispose()
    }

    @Test
    fun `Time Attack mismatch should apply time penalty`() = runTest {
        val pairCount = 4
        val mode = GameMode.TIME_ATTACK
        val initialState = MemoryGameLogic.createInitialState(pairCount, mode = mode)
        val card1 = initialState.cards[0]
        val card2 = initialState.cards.first { it.suit != card1.suit || it.rank != card1.rank }

        everySuspend { gameStateRepository.getSavedGameState() } returns (initialState to 30L)
        screenModel.handleIntent(GameIntent.StartGame(pairCount, mode = mode))

        screenModel.handleIntent(GameIntent.FlipCard(card1.id))
        screenModel.handleIntent(GameIntent.FlipCard(card2.id))

        val penalty = MemoryGameLogic.TIME_PENALTY_MISMATCH
        assertEquals(30L - penalty, screenModel.state.value.elapsedTimeSeconds)
        assertTrue(screenModel.state.value.showTimeLoss)
        
        testDispatcher.scheduler.advanceTimeBy(1501)
        assertFalse(screenModel.state.value.showTimeLoss)
        screenModel.onDispose()
    }

    @Test
    fun `Time Attack reaching 0 should trigger game over`() = runTest {
        val pairCount = 4
        val mode = GameMode.TIME_ATTACK
        screenModel.handleIntent(GameIntent.StartGame(pairCount, mode = mode))
        
        testDispatcher.scheduler.advanceTimeBy(16001)
        
        assertTrue(screenModel.state.value.game.isGameOver)
        verifySuspend { gameStateRepository.clearSavedGameState() }
        screenModel.onDispose()
    }

    // endregion

    // region Game Completion

    @Test
    fun `Game won should calculate final score and check for high score`() = runTest {
        val pairCount = 2
        statsFlow.value = GameStats(pairCount, bestScore = 50, bestTimeSeconds = 0L)
        everySuspend { gameStatsRepository.updateStats(any()) } returns Unit
        everySuspend { leaderboardRepository.addEntry(any()) } returns Unit

        screenModel.handleIntent(GameIntent.StartGame(pairCount))

        // Get the actual cards generated for this game
        val cards = screenModel.state.value.game.cards
        assertTrue(cards.isNotEmpty(), "Game should have cards")
        
        val groups = cards.groupBy { it.suit to it.rank }.values
        
        // Flip each pair
        groups.forEach { pair ->
            pair.forEach { card ->
                screenModel.handleIntent(GameIntent.FlipCard(card.id))
            }
        }

        val state = screenModel.state.value
        assertTrue(state.game.isGameOver, "Game should be over. Score: ${state.game.score}")
        assertTrue(state.isNewHighScore, "Should be a new high score")
        assertTrue(state.game.score > 50, "Score ${state.game.score} should be > 50")
        
        screenModel.onDispose()
        testDispatcher.scheduler.advanceUntilIdle()
        
        verifySuspend { leaderboardRepository.addEntry(any()) }
        verifySuspend { gameStateRepository.clearSavedGameState() }
    }

    // endregion

    // region Lifecycle & Persistence

    @Test
    fun `onDispose should save the current game state`() = runTest {
        val pairCount = 4
        screenModel.handleIntent(GameIntent.StartGame(pairCount))
        
        val cardId = screenModel.state.value.game.cards[0].id
        screenModel.handleIntent(GameIntent.FlipCard(cardId))
        
        val currentState = screenModel.state.value
        screenModel.onDispose()

        verifySuspend { gameStateRepository.saveGameState(currentState.game, currentState.elapsedTimeSeconds) }
    }

    @Test
    fun `SaveGame intent should trigger repository save`() = runTest {
        val pairCount = 4
        screenModel.handleIntent(GameIntent.StartGame(pairCount))
        
        screenModel.handleIntent(GameIntent.SaveGame)
        
        verifySuspend { gameStateRepository.saveGameState(any(), any()) }
        screenModel.onDispose()
    }

    // endregion
}
