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
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.domain.models.DifficultyType
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.SavedGame
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class GameComponentTest : BaseComponentTest() {
    private lateinit var component: DefaultGameComponent

    @BeforeTest
    override fun setUp() {
        super.setUp()
        everySuspend { context.shopItemRepository.getShopItems() } returns emptyList()
        every { context.playerEconomyRepository.selectedThemeId } returns MutableStateFlow("GEOMETRIC")
        every { context.playerEconomyRepository.selectedSkin } returns MutableStateFlow(CardSymbolTheme.CLASSIC)
    }

    private fun createComponent(
        lifecycle: Lifecycle,
        pairCount: Int = 8,
        mode: GameMode = GameMode.TIME_ATTACK,
        forceNewGame: Boolean = true,
    ): DefaultGameComponent =
        DefaultGameComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = context.appGraph,
            args = GameArgs(pairCount, mode, DifficultyType.CASUAL, forceNewGame),
            onBackClicked = {},
        )

    @Test
    fun `starts new game when checking initialization`() =
        runTest { lifecycle ->
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(false)
            component = createComponent(lifecycle)

            component.events.test {
                testDispatcher.scheduler.runCurrent()
                assertEquals(GameUiEvent.PlayDeal, awaitItem())
                cancelAndIgnoreRemainingEvents()
            }

            testDispatcher.scheduler.runCurrent()
            val state = component.state.value
            println("DEBUG: isPeeking=${state.isPeeking}, isPeekFeatureEnabled=${state.isPeekFeatureEnabled}")
            assertFalse(state.isPeeking, "Should not be peeking during initialization test. State: $state")
        }

    @Test
    fun `resumes saved game if available and valid`() =
        runTest { lifecycle ->
            val savedGame = MemoryGameState(pairCount = 8, mode = GameMode.TIME_ATTACK)
            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(savedGame, 100L)

            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            assertEquals(100L, component.state.value.elapsedTimeSeconds)
        }

    @Test
    fun `onFlipCard updates state and saves game`() =
        runTest { lifecycle ->
            val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
            val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
            val otherCards =
                (3..16).map { CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two) }
            val savedGame =
                MemoryGameState(
                    pairCount = 8,
                    mode = GameMode.TIME_ATTACK,
                    cards = (listOf(card1, card2) + otherCards).toPersistentList(),
                )

            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(savedGame, 60L)

            component = createComponent(lifecycle, forceNewGame = false)
            testDispatcher.scheduler.runCurrent()

            component.onFlipCard(1)
            testDispatcher.scheduler.runCurrent()
            component.onFlipCard(2)
            testDispatcher.scheduler.runCurrent()

            assertTrue(
                component.state.value.game.cards
                    .find { it.id == 1 }
                    ?.isFaceUp == true,
            )
            assertTrue(
                component.state.value.game.cards
                    .find { it.id == 2 }
                    ?.isFaceUp == true,
            )
            verifySuspend { context.gameStateRepository.saveGameState(any(), any()) }
        }

    @Test
    fun `timer counts down in Time Attack mode`() =
        runTest { lifecycle ->
            // Explicitly disable peek to ensure timer starts immediately
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(false)

            component = createComponent(lifecycle, pairCount = 8, mode = GameMode.TIME_ATTACK)
            testDispatcher.scheduler.runCurrent()
            testDispatcher.scheduler.advanceTimeBy(100L) // Account for initialization delay
            testDispatcher.scheduler.runCurrent()

            val startSeconds = component.state.value.elapsedTimeSeconds
            assertTrue(startSeconds > 0)

            component.state.test {
                // Poll for the change
                testDispatcher.scheduler.advanceTimeBy(2000)
                testDispatcher.scheduler.runCurrent()

                var latestState = awaitItem()
                while (latestState.elapsedTimeSeconds == startSeconds) {
                    latestState = awaitItem()
                }
                assertTrue(latestState.elapsedTimeSeconds < startSeconds, "Time should have decreased")
                cancelAndIgnoreRemainingEvents()
            }
        }

    @Test
    fun `peek feature delays timer and shows cards`() =
        runTest { lifecycle ->
            every { context.settingsRepository.isPeekEnabled } returns MutableStateFlow(true)

            component = createComponent(lifecycle)
            testDispatcher.scheduler.runCurrent()
            testDispatcher.scheduler.advanceTimeBy(50L) // Account for initialization delay
            testDispatcher.scheduler.runCurrent()

            // Should be in peeking state initially
            assertTrue(component.state.value.isPeeking)
            assertEquals(3, component.state.value.peekCountdown)

            val initialTime = component.state.value.elapsedTimeSeconds
            assertTrue(initialTime > 0)

            // Advance time and check countdown
            testDispatcher.scheduler.advanceTimeBy(1000)
            testDispatcher.scheduler.runCurrent()
            assertEquals(2, component.state.value.peekCountdown)

            testDispatcher.scheduler.advanceTimeBy(1000)
            testDispatcher.scheduler.runCurrent()
            assertEquals(1, component.state.value.peekCountdown)

            // Peek finished
            testDispatcher.scheduler.advanceTimeBy(1000)
            testDispatcher.scheduler.runCurrent()
            assertFalse(component.state.value.isPeeking)
            assertEquals(initialTime, component.state.value.elapsedTimeSeconds) // Time should haven't moved

            // Timer should start now
            testDispatcher.scheduler.advanceTimeBy(1000)
            testDispatcher.scheduler.runCurrent()
            assertEquals(initialTime - 1, component.state.value.elapsedTimeSeconds)
        }

    @Test
    fun `matching cards shows combo explosion for high multiplier`() =
        runTest { lifecycle ->
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
                    mode = GameMode.TIME_ATTACK,
                    cards = cards.toPersistentList(),
                    // High multiplier
                    comboMultiplier = 3,
                )

            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(gameWithCombo, 0L)

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
    fun `time attack match gains time`() =
        runTest { lifecycle ->
            val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace)
            val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace)
            val cards =
                listOf(card1, card2) +
                    (3..16).map {
                        CardState(id = it, suit = Suit.Diamonds, rank = Rank.Two)
                    }

            val savedGame =
                MemoryGameState(pairCount = 8, mode = GameMode.TIME_ATTACK, cards = cards.toPersistentList())

            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(savedGame, 30L)

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

    @Test
    fun `double down requires minimum pairs`() =
        runTest { lifecycle ->
            // Mock a state with Heat Mode but few pairs
            val card1 = CardState(id = 1, suit = Suit.Hearts, rank = Rank.Ace, isMatched = true)
            val card2 = CardState(id = 2, suit = Suit.Hearts, rank = Rank.Ace, isMatched = true)
            // Only 2 unmatched pairs remaining
            val card3 = CardState(id = 3, suit = Suit.Diamonds, rank = Rank.Two)
            val card4 = CardState(id = 4, suit = Suit.Diamonds, rank = Rank.Two)
            val card5 = CardState(id = 5, suit = Suit.Clubs, rank = Rank.Three)
            val card6 = CardState(id = 6, suit = Suit.Clubs, rank = Rank.Three)

            val cards = listOf(card1, card2, card3, card4, card5, card6)

            val gameWithHighCombo =
                MemoryGameState(
                    pairCount = 3,
                    mode = GameMode.TIME_ATTACK,
                    cards = cards.toPersistentList(),
                    comboMultiplier = 10, // Heat Mode active
                    isDoubleDownActive = false,
                )

            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(gameWithHighCombo, 0L)

            component = createComponent(lifecycle, forceNewGame = false, pairCount = 3)
            testDispatcher.scheduler.runCurrent()

            // Verify state
            assertTrue(component.state.value.isHeatMode)
            assertFalse(component.state.value.isDoubleDownAvailable)

            // Try enabling double down (should fail)
            component.onDoubleDown()
            testDispatcher.scheduler.runCurrent()
            assertFalse(component.state.value.game.isDoubleDownActive)

            // Now with enough pairs
            val moreCards =
                cards +
                    listOf(
                        CardState(id = 7, suit = Suit.Spades, rank = Rank.Four),
                        CardState(id = 8, suit = Suit.Spades, rank = Rank.Four),
                    )
            val gameWithMorePairs = gameWithHighCombo.copy(cards = moreCards.toPersistentList(), pairCount = 4)

            everySuspend { context.gameStateRepository.getSavedGameState() } returns
                SavedGame(gameWithMorePairs, 0L)

            val component2 = createComponent(lifecycle, forceNewGame = false, pairCount = 4)
            testDispatcher.scheduler.runCurrent()

            assertTrue(component2.state.value.isDoubleDownAvailable)
            component2.onDoubleDown()
            testDispatcher.scheduler.runCurrent()
            assertTrue(component2.state.value.game.isDoubleDownActive)
        }
}
