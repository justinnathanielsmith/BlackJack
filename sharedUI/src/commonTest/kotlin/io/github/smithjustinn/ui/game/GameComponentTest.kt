package io.github.smithjustinn.ui.game

import app.cash.turbine.test
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.everySuspend
import dev.mokkery.matcher.any
import dev.mokkery.verifySuspend
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameComponentTest : BaseComponentTest() {

    private lateinit var component: DefaultGameComponent

    private fun createComponent(
        lifecycle: Lifecycle,
        pairCount: Int = 8,
        mode: GameMode = GameMode.STANDARD,
        forceNewGame: Boolean = true,
    ): DefaultGameComponent = DefaultGameComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        appGraph = context.appGraph,
        args = GameArgs(pairCount, mode, forceNewGame),
        onBackClicked = {},
    )

    @Test
    fun `starts new game when checking initialization`() = runTest { lifecycle ->
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
    fun `resumes saved game if available and valid`() = runTest { lifecycle ->
        val savedGame = MemoryGameState(pairCount = 8, mode = GameMode.STANDARD)
        everySuspend { context.gameStateRepository.getSavedGameState() } returns
            (savedGame to 100L)

        component = createComponent(lifecycle, forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        assertEquals(100L, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `onFlipCard updates state and saves game`() = runTest { lifecycle ->
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
        val otherCards =
            (3..16).map { CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two) }
        val savedGame =
            MemoryGameState(
                pairCount = 8,
                mode = GameMode.STANDARD,
                cards = (listOf(card1, card2) + otherCards).toImmutableList(),
            )

        everySuspend { context.gameStateRepository.getSavedGameState() } returns
            (savedGame to 0L)

        component = createComponent(lifecycle, forceNewGame = false)
        testDispatcher.scheduler.runCurrent()

        component.onFlipCard(1)
        testDispatcher.scheduler.runCurrent()

        assertTrue(component.state.value.game.cards.find { it.id == 1 }?.isFaceUp == true)
        verifySuspend { context.gameStateRepository.saveGameState(any(), any()) }
    }

    @Test
    fun `timer ticks in Standard mode after initializing`() = runTest { lifecycle ->
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
    fun `timer counts down in Time Attack mode`() = runTest { lifecycle ->
        component = createComponent(lifecycle, pairCount = 8, mode = GameMode.TIME_ATTACK)
        testDispatcher.scheduler.runCurrent()

        val startSeconds = component.state.value.elapsedTimeSeconds
        assertTrue(startSeconds > 0)

        testDispatcher.scheduler.advanceTimeBy(1000)
        testDispatcher.scheduler.runCurrent()
        assertEquals(startSeconds - 1, component.state.value.elapsedTimeSeconds)
    }

    @Test
    fun `peek feature delays timer and shows cards`() = runTest { lifecycle ->
        every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(true)

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
    fun `matching cards shows combo explosion for high multiplier`() = runTest { lifecycle ->
        // Mock a state with high multiplier
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
        val cards =
            listOf(card1, card2) +
                (3..16).map {
                    CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two)
                }

        val gameWithCombo =
            MemoryGameState(
                pairCount = 8,
                mode = GameMode.STANDARD,
                cards = cards.toImmutableList(),
                // High multiplier
                comboMultiplier = 3,
            )

        everySuspend { context.gameStateRepository.getSavedGameState() } returns
            (gameWithCombo to 0L)

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
    fun `time attack match gains time`() = runTest { lifecycle ->
        val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
        val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
        val cards =
            listOf(card1, card2) +
                (3..16).map {
                    CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two)
                }

        val savedGame =
            MemoryGameState(pairCount = 8, mode = GameMode.TIME_ATTACK, cards = cards.toImmutableList())

        everySuspend { context.gameStateRepository.getSavedGameState() } returns
            (savedGame to 30L)

        component =
            createComponent(
                lifecycle,
                forceNewGame = false,
                mode = GameMode.TIME_ATTACK,
            )
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
