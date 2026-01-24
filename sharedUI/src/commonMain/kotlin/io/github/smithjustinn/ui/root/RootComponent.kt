package io.github.smithjustinn.ui.root

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.value.Value
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.game.DefaultGameComponent
import io.github.smithjustinn.ui.game.GameComponent
import io.github.smithjustinn.ui.settings.DefaultSettingsComponent
import io.github.smithjustinn.ui.settings.SettingsComponent
import io.github.smithjustinn.ui.start.DefaultStartComponent
import io.github.smithjustinn.ui.start.StartComponent
import io.github.smithjustinn.ui.stats.DefaultStatsComponent
import io.github.smithjustinn.ui.stats.StatsComponent
import kotlinx.serialization.Serializable

interface RootComponent {
    val childStack: Value<ChildStack<*, Child>>
    val backHandler: com.arkivanov.essenty.backhandler.BackHandler

    fun pop()

    sealed class Child {
        class Start(val component: StartComponent) : Child()
        class Game(val component: GameComponent) : Child()
        class Settings(val component: SettingsComponent) : Child()
        class Stats(val component: StatsComponent) : Child()
    }
}

class DefaultRootComponent(componentContext: ComponentContext, private val appGraph: AppGraph) :
        RootComponent, ComponentContext by componentContext {

    private val navigation = StackNavigation<Config>()

    override val childStack: Value<ChildStack<*, RootComponent.Child>> =
            childStack(
                    source = navigation,
                    serializer = Config.serializer(),
                    initialConfiguration = Config.Start,
                    handleBackButton = true,
                    childFactory = ::createChild,
            )

    override fun pop() {
        navigation.pop()
    }

    private fun createChild(
            config: Config,
            componentContext: ComponentContext
    ): RootComponent.Child =
            when (config) {
                is Config.Start ->
                        RootComponent.Child.Start(
                                DefaultStartComponent(
                                        componentContext = componentContext,
                                        appGraph = appGraph,
                                        onNavigateToGame =
                                                @OptIn(
                                                        com.arkivanov.decompose
                                                                .DelicateDecomposeApi::class
                                                )
                                                { pairs, mode, forceNewGame ->
                                                    navigation.push(
                                                            Config.Game(pairs, mode, forceNewGame)
                                                    )
                                                },
                                        onNavigateToSettings =
                                                @OptIn(
                                                        com.arkivanov.decompose
                                                                .DelicateDecomposeApi::class
                                                )
                                                {
                                                    navigation.push(Config.Settings)
                                                },
                                        onNavigateToStats =
                                                @OptIn(
                                                        com.arkivanov.decompose
                                                                .DelicateDecomposeApi::class
                                                )
                                                {
                                                    navigation.push(Config.Stats)
                                                }
                                )
                        )
                is Config.Game ->
                        RootComponent.Child.Game(
                                DefaultGameComponent(
                                        componentContext = componentContext,
                                        appGraph = appGraph,
                                        pairCount = config.pairs,
                                        mode = config.mode,
                                        forceNewGame = config.forceNewGame,
                                        onBackClicked = navigation::pop
                                )
                        )
                is Config.Settings ->
                        RootComponent.Child.Settings(
                                DefaultSettingsComponent(
                                        componentContext = componentContext,
                                        appGraph = appGraph,
                                        onBackClicked = navigation::pop
                                )
                        )
                is Config.Stats ->
                        RootComponent.Child.Stats(
                                DefaultStatsComponent(
                                        componentContext = componentContext,
                                        appGraph = appGraph,
                                        onBackClicked = navigation::pop
                                )
                        )
            }

    @Serializable
    private sealed interface Config {
        @Serializable data object Start : Config

        @Serializable
        data class Game(val pairs: Int, val mode: GameMode, val forceNewGame: Boolean) : Config

        @Serializable data object Settings : Config

        @Serializable data object Stats : Config
    }
}
