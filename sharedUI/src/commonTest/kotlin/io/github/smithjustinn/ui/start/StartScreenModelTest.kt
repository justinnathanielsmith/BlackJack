package io.github.smithjustinn.ui.start

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StartScreenModelTest {

    // region Dependencies & Setup
    private val gameStateRepository: GameStateRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val logger: Logger = Logger(StaticConfig())
    private lateinit var screenModel: StartScreenModel
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { settingsRepository.cardSymbolTheme } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        
        screenModel = StartScreenModel(gameStateRepository, settingsRepository, logger)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }
    // endregion

    // region Initial State
    @Test
    fun `initial state is correct`() = runTest {
        screenModel.state.test {
            val state = awaitItem()
            assertEquals(DifficultyLevel.defaultLevels.size, state.difficulties.size)
            assertEquals(DifficultyLevel.defaultLevels[1].pairs, state.selectedDifficulty.pairs)
            assertFalse(state.hasSavedGame)
            assertEquals(GameMode.STANDARD, state.selectedMode)
        }
    }
    // endregion

    // region Intent Handling: Selection
    @Test
    fun `SelectDifficulty intent updates state`() = runTest {
        val newDifficulty = DifficultyLevel.defaultLevels[0]
        screenModel.state.test {
            awaitItem() // Initial state
            screenModel.handleIntent(DifficultyIntent.SelectDifficulty(newDifficulty))
            val newState = awaitItem()
            assertEquals(newDifficulty.pairs, newState.selectedDifficulty.pairs)
        }
    }

    @Test
    fun `SelectMode intent updates state`() = runTest {
        val newMode = GameMode.TIME_ATTACK
        screenModel.state.test {
            awaitItem() // Initial state
            screenModel.handleIntent(DifficultyIntent.SelectMode(newMode))
            val newState = awaitItem()
            assertEquals(newMode, newState.selectedMode)
        }
    }
    // endregion

    // region Intent Handling: Game State
    @Test
    fun `CheckSavedGame intent updates state when game exists`() = runTest {
        val savedGame = MemoryGameState(pairCount = 12, mode = GameMode.TIME_ATTACK, isGameOver = false)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 100L)

        screenModel.state.test {
            awaitItem() // Initial state
            screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
            
            val newState = awaitItem()
            assertTrue(newState.hasSavedGame)
            assertEquals(12, newState.savedGamePairCount)
            assertEquals(GameMode.TIME_ATTACK, newState.savedGameMode)
            
            verifySuspend { gameStateRepository.getSavedGameState() }
        }
    }

    @Test
    fun `CheckSavedGame intent updates state when no game exists`() = runTest {
        everySuspend { gameStateRepository.getSavedGameState() } returns null

        screenModel.state.test {
            awaitItem() // Initial state
            screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
            
            testDispatcher.scheduler.advanceUntilIdle()
            expectNoEvents()
            
            assertFalse(screenModel.state.value.hasSavedGame)
            verifySuspend { gameStateRepository.getSavedGameState() }
        }
    }
    // endregion

    // region Intent Handling: Navigation
    @Test
    fun `StartGame intent triggers navigation event`() = runTest {
        screenModel.events.test {
            screenModel.handleIntent(DifficultyIntent.StartGame(10, GameMode.TIME_ATTACK))
            
            val event = awaitItem() as DifficultyUiEvent.NavigateToGame
            assertEquals(10, event.pairs)
            assertEquals(GameMode.TIME_ATTACK, event.mode)
            assertTrue(event.forceNewGame)
        }
    }

    @Test
    fun `ResumeGame intent triggers navigation event if saved game exists`() = runTest {
        val savedGame = MemoryGameState(pairCount = 12, mode = GameMode.TIME_ATTACK, isGameOver = false)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 100L)

        // Update state first
        screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.events.test {
            screenModel.handleIntent(DifficultyIntent.ResumeGame)
            
            val event = awaitItem() as DifficultyUiEvent.NavigateToGame
            assertEquals(12, event.pairs)
            assertEquals(GameMode.TIME_ATTACK, event.mode)
            assertFalse(event.forceNewGame)
        }
    }

    @Test
    fun `ResumeGame intent does not trigger navigation event if no saved game`() = runTest {
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        
        screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
        testDispatcher.scheduler.advanceUntilIdle()

        screenModel.events.test {
            screenModel.handleIntent(DifficultyIntent.ResumeGame)
            expectNoEvents()
        }
    }
    // endregion
}
