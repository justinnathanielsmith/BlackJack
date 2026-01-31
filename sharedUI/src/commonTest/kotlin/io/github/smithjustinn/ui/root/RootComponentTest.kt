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
}
