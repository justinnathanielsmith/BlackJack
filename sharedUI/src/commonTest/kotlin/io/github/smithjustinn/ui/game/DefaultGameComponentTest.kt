package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DefaultGameComponentTest : BaseComponentTest() {
    private lateinit var component: DefaultGameComponent

    @BeforeTest
    override fun setUp() {
        super.setUp()
        // Default mocks using MutableStateFlow to match StateFlow requirement
        every { context.settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(true)
        every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(false)
        every { context.settingsRepository.isMusicEnabled } returns MutableStateFlow(true)
        every { context.settingsRepository.isSoundEnabled } returns MutableStateFlow(true)
        every { context.playerEconomyRepository.selectedTheme } returns MutableStateFlow(CardBackTheme.GEOMETRIC)
        every { context.playerEconomyRepository.selectedThemeId } returns MutableStateFlow("GEOMETRIC")
        every { context.playerEconomyRepository.selectedSkin } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
        everySuspend { context.shopItemRepository.getShopItems() } returns emptyList()
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

    @Test
    fun `toggling audio updates settings repository`() =
        runTest { lifecycle ->
            // Given
            val isMusicEnabledFlow = MutableStateFlow(true)
            val isSoundEnabledFlow = MutableStateFlow(true)
            every { context.settingsRepository.isMusicEnabled } returns isMusicEnabledFlow
            every { context.settingsRepository.isSoundEnabled } returns isSoundEnabledFlow
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(false)

            // Mock setters
            everySuspend { context.settingsRepository.setMusicEnabled(any()) } returns Unit
            everySuspend { context.settingsRepository.setSoundEnabled(any()) } returns Unit

            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            // When
            component.onToggleAudio()
            testDispatcher.scheduler.runCurrent()

            // Then
            verifySuspend {
                context.settingsRepository.setMusicEnabled(false)
                context.settingsRepository.setSoundEnabled(false)
            }
        }

    @Test
    fun `completing walkthrough updates settings repository`() =
        runTest { lifecycle ->
            // Given
            every { context.settingsRepository.isWalkthroughCompleted } returns MutableStateFlow(false)
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(false)
            everySuspend { context.settingsRepository.setWalkthroughCompleted(any()) } returns Unit

            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            // When
            component.onWalkthroughAction(isComplete = true)
            testDispatcher.scheduler.runCurrent()

            // Then
            verifySuspend {
                context.settingsRepository.setWalkthroughCompleted(true)
            }
        }

    @Test
    fun `settings changes are reflected in state`() =
        runTest { lifecycle ->
            // Given
            val isPeekEnabledFlow = MutableStateFlow(false)
            val isMusicEnabledFlow = MutableStateFlow(true)
            val isSoundEnabledFlow = MutableStateFlow(true)

            every { context.settingsRepository.isPeekEnabled } returns isPeekEnabledFlow
            every { context.settingsRepository.isMusicEnabled } returns isMusicEnabledFlow
            every { context.settingsRepository.isSoundEnabled } returns isSoundEnabledFlow

            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            component.state.test {
                val initialState = awaitItem()

                // When
                // Toggle values to ensure we trigger a change regardless of initial state
                val newPeek = !initialState.isPeekFeatureEnabled
                val newMusic = !initialState.isMusicEnabled
                val newSound = !initialState.isSoundEnabled

                isPeekEnabledFlow.emit(newPeek)
                isMusicEnabledFlow.emit(newMusic)
                isSoundEnabledFlow.emit(newSound)

                testDispatcher.scheduler.runCurrent()

                // Then
                // We expect at least one state update. Since we emit multiple changes,
                // and they might be conflated or sequential depending on dispatcher,
                // we should loop until we match expectation or timeout.

                var currentState = awaitItem()
                while (currentState.isPeekFeatureEnabled != newPeek ||
                       currentState.isMusicEnabled != newMusic ||
                       currentState.isSoundEnabled != newSound) {
                    currentState = awaitItem()
                }

                assertEquals(newPeek, currentState.isPeekFeatureEnabled)
                assertEquals(newMusic, currentState.isMusicEnabled)
                assertEquals(newSound, currentState.isSoundEnabled)
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
                    difficulty = DifficultyType.CASUAL,
                    seed = null,
                    forceNewGame = forceNewGame,
                ),
            onBackClicked = {},
        )
}
