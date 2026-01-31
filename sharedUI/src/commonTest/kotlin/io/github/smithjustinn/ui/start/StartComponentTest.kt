package io.github.smithjustinn.ui.start

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.everySuspend
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StartComponentTest : BaseComponentTest() {
    private lateinit var component: DefaultStartComponent
    private var navigatedToGame: Triple<Int, GameMode, Boolean>? = null
    private var navigatedToSettings = false
    private var navigatedToStats = false

    @Test
    fun `initial state is correct`() =
        runTest { lifecycle ->
            component = createDefaultComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.state.test {
                val state = awaitItem()
                assertEquals(DifficultyLevel.defaultLevels.size, state.difficulties.size)
                assertEquals(DifficultyLevel.defaultLevels[1].pairs, state.selectedDifficulty.pairs)
                assertFalse(state.hasSavedGame)
                assertEquals(GameMode.TIME_ATTACK, state.selectedMode)
            }
        }

    @Test
    fun `onDifficultySelected updates state`() =
        runTest { lifecycle ->
            component = createDefaultComponent(lifecycle)
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
    fun `onModeSelected updates state`() =
        runTest { lifecycle ->
            component = createDefaultComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            val newMode = GameMode.DAILY_CHALLENGE
            component.state.test {
                awaitItem() // Initial state
                component.onModeSelected(newMode)
                val newState = awaitItem()
                assertEquals(newMode, newState.selectedMode)
            }
        }

    @Test
    fun `Checks saved game on init and updates state when game exists`() =
        runTest { lifecycle ->
            val savedGame =
                MemoryGameState(pairCount = 12, mode = GameMode.TIME_ATTACK, isGameOver = false)
            everySuspend { context.gameStateRepository.getSavedGameState() } returns (savedGame to 100L)

            component = createDefaultComponent(lifecycle)

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
    fun `onStartGame triggers navigation callback`() =
        runTest { lifecycle ->
            component = createDefaultComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.onDifficultySelected(DifficultyLevel.defaultLevels[2]) // 10 pairs
            component.onModeSelected(GameMode.DAILY_CHALLENGE)
            testDispatcher.scheduler.runCurrent()

            component.onStartGame()
            assertEquals(Triple(10, GameMode.DAILY_CHALLENGE, true), navigatedToGame)
        }

    @Test
    fun `onResumeGame triggers navigation callback if saved game exists`() =
        runTest { lifecycle ->
            val savedGame =
                MemoryGameState(pairCount = 12, mode = GameMode.TIME_ATTACK, isGameOver = false)
            everySuspend { context.gameStateRepository.getSavedGameState() } returns (savedGame to 100L)

            component = createDefaultComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()

            component.onResumeGame()
            assertEquals(Triple(12, GameMode.TIME_ATTACK, false), navigatedToGame)
        }

    private fun createDefaultComponent(lifecycle: Lifecycle): DefaultStartComponent =
        DefaultStartComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = context.appGraph,
            onNavigateToGame = { pairs, mode, isNewGame ->
                navigatedToGame = Triple(pairs, mode, isNewGame)
            },
            onNavigateToSettings = { navigatedToSettings = true },
            onNavigateToStats = { navigatedToStats = true },
        )
}
