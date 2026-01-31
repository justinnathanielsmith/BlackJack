package io.github.smithjustinn.ui.root

import com.arkivanov.decompose.DefaultComponentContext
import io.github.smithjustinn.test.BaseComponentTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RootComponentTest : BaseComponentTest() {
    @Test
    fun `initial child is Start`() =
        runTest { lifecycle ->
            val root =
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    appGraph = context.appGraph,
                )

            assertTrue(root.childStack.value.active.instance is RootComponent.Child.Start)
        }

    @Test
    fun `navigating to Game updates stack`() =
        runTest { lifecycle ->
            val root =
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    appGraph = context.appGraph,
                )

            val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
            startChild.component.onStartGame()

            assertTrue(root.childStack.value.active.instance is RootComponent.Child.Game)
        }

    @Test
    fun `navigating to Settings updates stack`() =
        runTest { lifecycle ->
            val root =
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    appGraph = context.appGraph,
                )

            val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
            startChild.component.onSettingsClick()

            assertTrue(root.childStack.value.active.instance is RootComponent.Child.Settings)
        }

    @Test
    fun `navigating to Stats updates stack`() =
        runTest { lifecycle ->
            val root =
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    appGraph = context.appGraph,
                )

            val startChild = root.childStack.value.active.instance as RootComponent.Child.Start
            startChild.component.onStatsClick()

            assertTrue(root.childStack.value.active.instance is RootComponent.Child.Stats)
        }

    @Test
    fun `back from BuyIn returns to Start even with history`() =
        runTest { lifecycle ->
            val root =
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    appGraph = context.appGraph,
                )

            // Simulate navigation: Start -> Game -> BuyIn
            val startChild = root.childStack.value.active.instance as RootComponent.Child.Start

            // StartComponent interaction to trigger navigation
            startChild.component.onModeSelected(io.github.smithjustinn.domain.models.GameMode.HIGH_ROLLER)
            startChild.component.onStartGame()

            // Note: Start component logic pushes BuyIn directly for High Roller, so stack is Start -> BuyIn

            // Re-simulate: Start -> BuyIn (High Roller Start)
            assertTrue(root.childStack.value.active.instance is RootComponent.Child.BuyIn)

            val buyInChild = root.childStack.value.active.instance as RootComponent.Child.BuyIn
            buyInChild.component.onStartRound()

            // Now stack is Start -> BuyIn -> Game
            assertTrue(root.childStack.value.active.instance is RootComponent.Child.Game)
            val gameChild = root.childStack.value.active.instance as RootComponent.Child.Game

            // Simulate cycling stage which pushes another BuyIn
            gameChild.component.onCycleStage(io.github.smithjustinn.domain.models.CircuitStage.SEMI_FINAL, 200)

            // Now stack is Start -> BuyIn -> Game -> BuyIn
            assertTrue(root.childStack.value.active.instance is RootComponent.Child.BuyIn)
            val secondBuyInChild = root.childStack.value.active.instance as RootComponent.Child.BuyIn

            // Trigger Back to Lobby
            secondBuyInChild.component.onBack()

            // Should be back at Start
            assertTrue(root.childStack.value.active.instance is RootComponent.Child.Start)
        }
}
