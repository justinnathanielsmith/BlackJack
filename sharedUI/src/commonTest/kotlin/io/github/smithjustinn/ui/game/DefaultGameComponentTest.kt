package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultGameComponentTest : BaseComponentTest() {
    private lateinit var component: DefaultGameComponent

    @BeforeTest
    override fun setUp() {
        super.setUp()
        // Default mocks using MutableStateFlow to match StateFlow requirement
        every { context.settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(true)
        every { context.settingsRepository.isMusicEnabled } returns MutableStateFlow(true)
        every { context.settingsRepository.isSoundEnabled } returns MutableStateFlow(true)
        every { context.settingsRepository.cardBackTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { context.settingsRepository.cardSymbolTheme } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        every { context.settingsRepository.areSuitsMultiColored } returns MutableStateFlow(true)
    }

    @Test
    fun `starting new game with peek enabled triggers peek sequence`() =
        runTest { lifecycle ->
            // Given
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(true)
            everySuspend { context.gameStateRepository.getSavedGameState() } returns null

            // When
            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            // Then
            component.state.test {
                // Initial state
                val initialState = awaitItem()

                // Should eventually reach peeking state
                var currentState = initialState
                while (!(currentState.isPeekFeatureEnabled && currentState.isPeeking)) {
                    currentState = awaitItem()
                    if (currentState.elapsedTimeSeconds > 0) {
                        break // Optimization: if timer started, we missed peek or it didn't happen
                    }
                }

                assertTrue(currentState.isPeeking, "Should have entered peek state")
                assertTrue(currentState.isPeekFeatureEnabled, "Peek feature should be enabled")
            }
        }

    @Test
    fun `resuming game does NOT trigger peek sequence`() =
        runTest { lifecycle ->
            // Given
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(true)

            // Mock saved game returns a game (Resume scenario)
            val savedState = MemoryGameState(mode = GameMode.TIME_ATTACK, pairCount = 8)
            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(savedState, 10L)

            // When
            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            // Then
            component.state.test {
                // Initial state should load saved game
                val initialState = awaitItem()
                assertTrue(initialState.elapsedTimeSeconds == 10L, "Should have loaded saved time")
                assertFalse(initialState.isPeeking, "Should NOT be peeking when resuming game")

                cancelAndIgnoreRemainingEvents()
            }
        }

    private fun createComponent(
        lifecycle: Lifecycle,
        forceNewGame: Boolean,
    ): DefaultGameComponent =
        DefaultGameComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = context.appGraph,
            args =
                GameArgs(
                    pairCount = 8,
                    mode = GameMode.TIME_ATTACK,
                    seed = null,
                    forceNewGame = forceNewGame,
                ),
            onBackClicked = {},
        )
}
