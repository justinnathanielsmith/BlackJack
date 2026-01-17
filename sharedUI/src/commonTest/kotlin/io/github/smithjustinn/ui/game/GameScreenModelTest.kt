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
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
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
    private val statsFlow = MutableStateFlow<GameStats?>(null)

    private lateinit var screenModel: GameScreenModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        isPeekEnabledFlow.value = false
        statsFlow.value = null
        
        every { settingsRepository.isPeekEnabled } returns isPeekEnabledFlow
        every { gameStatsRepository.getStatsForDifficulty(any()) } returns statsFlow
        
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        everySuspend { gameStateRepository.saveGameState(any(), any()) } returns Unit
        everySuspend { gameStateRepository.clearSavedGameState() } returns Unit
        
        screenModel = GameScreenModel(
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
    }

    @AfterTest
    fun tearDown() {
        screenModel.onDispose()
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
            // StateFlow emits current value first, then updates.
            // We wait for the state that has the stats.
            var state = awaitItem()
            if (state.bestScore == 0) {
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

    // endregion

    // region Gameplay Logic

    @Test
    fun `FlipCard should ignore clicks when peeking`() = runTest {
        val pairCount = 4
        isPeekEnabledFlow.value = true
        
        screenModel.handleIntent(GameIntent.StartGame(pairCount))
        // We are now in peeking state
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

        // First match (combo 1 -> 2)
        screenModel.handleIntent(GameIntent.FlipCard(card1.id))
        screenModel.handleIntent(GameIntent.FlipCard(card2.id))
        
        // Second match (combo 2 -> 3)
        screenModel.handleIntent(GameIntent.FlipCard(card3.id))
        screenModel.handleIntent(GameIntent.FlipCard(card4.id))

        assertTrue(screenModel.state.value.showComboExplosion)
        
        testScheduler.advanceTimeBy(1001)
        assertFalse(screenModel.state.value.showComboExplosion)
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
        
        testScheduler.advanceTimeBy(1501)
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
        
        testScheduler.advanceTimeBy(1501)
        assertFalse(screenModel.state.value.showTimeLoss)
        screenModel.onDispose()
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
