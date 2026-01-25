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
import io.github.smithjustinn.utils.componentScope
import kotlinx.coroutines.launch
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
    RootComponent,
    ComponentContext by componentContext {
    private val navigation = StackNavigation<Config>()
    private val logger = appGraph.logger

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

    init {
        val scope = componentContext.lifecycle.componentScope(appGraph.coroutineDispatchers.mainImmediate)
        scope.launch {
            DeepLinkHandler.deepLinks.collect { url ->
                handleDeepLink(url)
            }
        }
    }

    // Url format: memorymatch://game?mode=STANDARD&pairs=8&seed=12345
    private fun handleDeepLink(url: String) {
        if (!url.startsWith("memorymatch://game")) return

        try {
            val modeStr = url.getQueryParameter("mode")
            val pairsStr = url.getQueryParameter("pairs")
            val seedStr = url.getQueryParameter("seed")

            val mode = modeStr?.let { GameMode.valueOf(it) } ?: GameMode.STANDARD
            val pairs = pairsStr?.toIntOrNull() ?: 8
            val seed = seedStr?.toLongOrNull()

            @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class)
            navigation.push(Config.Game(pairs, mode, forceNewGame = true, seed = seed))
        } catch (e: Exception) {
            logger.e(e) { "Error handling deep link: $url" }
        }
    }

    private fun createChild(config: Config, componentContext: ComponentContext): RootComponent.Child = when (config) {
        is Config.Start -> RootComponent.Child.Start(createStartComponent(componentContext))
        is Config.Game -> RootComponent.Child.Game(createGameComponent(config, componentContext))
        is Config.Settings -> RootComponent.Child.Settings(createSettingsComponent(componentContext))
        is Config.Stats -> RootComponent.Child.Stats(createStatsComponent(componentContext))
    }

    private fun createStartComponent(componentContext: ComponentContext): StartComponent = DefaultStartComponent(
        componentContext = componentContext,
        appGraph = appGraph,
        onNavigateToGame =
        @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class) { pairs, mode, forceNewGame ->
            navigation.push(Config.Game(pairs, mode, forceNewGame, null))
        },
        onNavigateToSettings =
        @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class) {
            navigation.push(Config.Settings)
        },
        onNavigateToStats =
        @OptIn(com.arkivanov.decompose.DelicateDecomposeApi::class) {
            navigation.push(Config.Stats)
        },
    )

    private fun createGameComponent(config: Config.Game, componentContext: ComponentContext): GameComponent =
        DefaultGameComponent(
            componentContext = componentContext,
            appGraph = appGraph,
            pairCount = config.pairs,
            mode = config.mode,
            forceNewGame = config.forceNewGame,
            seed = config.seed,
            onBackClicked = navigation::pop,
        )

    private fun createSettingsComponent(componentContext: ComponentContext): SettingsComponent =
        DefaultSettingsComponent(
            componentContext = componentContext,
            appGraph = appGraph,
            onBackClicked = navigation::pop,
        )

    private fun createStatsComponent(componentContext: ComponentContext): StatsComponent = DefaultStatsComponent(
        componentContext = componentContext,
        appGraph = appGraph,
        onBackClicked = navigation::pop,
    )

    @Serializable
    private sealed interface Config {
        @Serializable data object Start : Config

        @Serializable
        data class Game(val pairs: Int, val mode: GameMode, val forceNewGame: Boolean, val seed: Long?) : Config

        @Serializable data object Settings : Config

        @Serializable data object Stats : Config
    }
}

private fun String.getQueryParameter(key: String): String? {
    val queryStart = indexOf('?')
    if (queryStart == -1) return null
    val query = substring(queryStart + 1)
    val pairs = query.split('&')
    for (pair in pairs) {
        val parts = pair.split('=')
        if (parts.size == 2 && parts[0] == key) {
            return parts[1]
        }
    }
    return null
}
