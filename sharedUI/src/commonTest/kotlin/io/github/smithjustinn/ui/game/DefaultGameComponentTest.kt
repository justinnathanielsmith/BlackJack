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

            // Mock Repository to return null (New Game scenario)
            // context.appGraph.getSavedGameUseCase uses context.gameStateRepository internally
            everySuspend { context.gameStateRepository.getSavedGameState() } returns null

            // When
            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            // Then
            component.state.test {
                // Need to wait for coroutines
                var foundPeekState = false
                try {
                    // Peek should start after initialization
                    // We check a few items
                    var attempts = 0
                    while (!foundPeekState && attempts < 10) {
                        val state = awaitItem()
                        if (state.isPeekFeatureEnabled && state.isPeeking) {
                            foundPeekState = true
                        }
                        attempts++
                        if (state.elapsedTimeSeconds > 0) {
                            // Game started without peek (timer ticking)
                            break
                        }
                    }
                } catch (e: Exception) {
                    // Timeout or explicit failure
                }
                assertTrue(foundPeekState, "Should have entered peek state")
            }
        }

    @Test
    fun `resuming game does NOT trigger peek sequence`() =
        runTest { lifecycle ->
            // Given
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(true)

            // Mock saved game returns a game (Resume scenario)
            val savedState = MemoryGameState(mode = GameMode.TIME_ATTACK, pairCount = 8)
            everySuspend { context.gameStateRepository.getSavedGameState() } returns (savedState to 10L)

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
                    circuitStage = null,
                    bankedScore = 0,
                    currentWager = 0,
                ),
            onBackClicked = {},
            onCycleStage = { _, _ -> },
        )
}
