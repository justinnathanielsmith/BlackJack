package io.github.smithjustinn.ui.start

import app.cash.turbine.test
import co.touchlab.kermit.Logger
import co.touchlab.kermit.StaticConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.mock
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.SettingsRepository
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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
class StartComponentTest {

    private val gameStateRepository: GameStateRepository = mock()
    private val settingsRepository: SettingsRepository = mock()
    private val logger: Logger = Logger(StaticConfig())
    private val appGraph: AppGraph = mock()
    
    private lateinit var component: DefaultStartComponent
    private val testDispatcher = StandardTestDispatcher()
    private var lifecycle: LifecycleRegistry? = null
    private var navigatedToGame: Triple<Int, GameMode, Boolean>? = null
    private var navigatedToSettings = false
    private var navigatedToStats = false

    private fun runStartTest(block: suspend TestScope.() -> Unit) = runTest(testDispatcher) {
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

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        
        every { appGraph.gameStateRepository } returns gameStateRepository
        every { appGraph.settingsRepository } returns settingsRepository
        every { appGraph.logger } returns logger
        
        every { settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { settingsRepository.cardSymbolTheme } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        everySuspend { gameStateRepository.getSavedGameState() } returns null
        
        every { appGraph.coroutineDispatchers } returns CoroutineDispatchers(
            main = testDispatcher,
            mainImmediate = testDispatcher,
            io = testDispatcher,
            default = testDispatcher
        )
    }

    @AfterTest
    fun tearDown() {
        lifecycle?.onDestroy()
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is correct`() = runStartTest {
        component = createDefaultComponent()
        testDispatcher.scheduler.runCurrent()

        component.state.test {
            val state = awaitItem()
            assertEquals(DifficultyLevel.defaultLevels.size, state.difficulties.size)
            assertEquals(DifficultyLevel.defaultLevels[1].pairs, state.selectedDifficulty.pairs)
            assertFalse(state.hasSavedGame)
            assertEquals(GameMode.STANDARD, state.selectedMode)
        }
    }

    @Test
    fun `onDifficultySelected updates state`() = runStartTest {
        component = createDefaultComponent()
        testDispatcher.scheduler.runCurrent()
        
        val newDifficulty = DifficultyLevel.defaultLevels[0]
        component.state.test {
            awaitItem() // Initial state
            component.onDifficultySelected(newDifficulty)
            val newState = awaitItem()
            assertEquals(newDifficulty.pairs, newState.selectedDifficulty.pairs)
        }
    }

    @Test
    fun `onModeSelected updates state`() = runStartTest {
        component = createDefaultComponent()
        testDispatcher.scheduler.runCurrent()
        
        val newMode = GameMode.TIME_ATTACK
        component.state.test {
            awaitItem() // Initial state
            component.onModeSelected(newMode)
            val newState = awaitItem()
            assertEquals(newMode, newState.selectedMode)
        }
    }

    @Test
    fun `Checks saved game on init and updates state when game exists`() = runStartTest {
        val savedGame = MemoryGameState(pairCount = 12, mode = GameMode.TIME_ATTACK, isGameOver = false)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 100L)

        component = createDefaultComponent()
        // No need to skip item if we haven't run current yet, but let's be safe
        
        component.state.test {
            // The initial state before checkSavedGame finishes
            val initial = awaitItem()
            assertFalse(initial.hasSavedGame)
            
            testDispatcher.scheduler.runCurrent()
            
            val updated = awaitItem()
            assertTrue(updated.hasSavedGame)
            assertEquals(12, updated.savedGamePairCount)
            assertEquals(GameMode.TIME_ATTACK, updated.savedGameMode)
        }
    }

    @Test
    fun `onStartGame triggers navigation callback`() = runStartTest {
        component = createDefaultComponent()
        testDispatcher.scheduler.runCurrent()
        
        component.onDifficultySelected(DifficultyLevel.defaultLevels[2]) // 10 pairs
        component.onModeSelected(GameMode.TIME_ATTACK)
        testDispatcher.scheduler.runCurrent()
        
        component.onStartGame()
        assertEquals(Triple(10, GameMode.TIME_ATTACK, true), navigatedToGame)
    }

    @Test
    fun `onResumeGame triggers navigation callback if saved game exists`() = runStartTest {
        val savedGame = MemoryGameState(pairCount = 12, mode = GameMode.TIME_ATTACK, isGameOver = false)
        everySuspend { gameStateRepository.getSavedGameState() } returns (savedGame to 100L)

        component = createDefaultComponent()
        testDispatcher.scheduler.runCurrent()

        component.onResumeGame()
        assertEquals(Triple(12, GameMode.TIME_ATTACK, false), navigatedToGame)
    }

    private fun createDefaultComponent(): DefaultStartComponent {
        return DefaultStartComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle!!),
            appGraph = appGraph,
            onNavigateToGame = { pairs, mode, isNewGame ->
                navigatedToGame = Triple(pairs, mode, isNewGame)
            },
            onNavigateToSettings = { navigatedToSettings = true },
            onNavigateToStats = { navigatedToStats = true }
        )
    }

}
