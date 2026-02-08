package io.github.smithjustinn

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.theme.AppTheme
import io.github.smithjustinn.theme.LocalCardTheme
import io.github.smithjustinn.ui.debug.DebugContent
import io.github.smithjustinn.ui.game.GameContent
import io.github.smithjustinn.ui.root.RootComponent
import io.github.smithjustinn.ui.settings.SettingsContent
import io.github.smithjustinn.ui.shop.ShopContent
import io.github.smithjustinn.ui.splash.SplashScreen
import io.github.smithjustinn.ui.start.StartContent
import io.github.smithjustinn.ui.stats.StatsContent
import kotlinx.coroutines.flow.combine

private const val SPLASH_ANIMATION_DURATION = 1000

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun App(
    root: RootComponent,
    appGraph: AppGraph,
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
) = AppTheme(onThemeChanged) {
    var showSplash by remember { mutableStateOf(true) }

    AnimatedContent(
        targetState = showSplash,
        transitionSpec = {
            fadeIn(animationSpec = tween(SPLASH_ANIMATION_DURATION)) togetherWith
                fadeOut(animationSpec = tween(SPLASH_ANIMATION_DURATION))
        },
        label = "SplashTransition",
    ) { show ->
        if (show) {
            SplashScreen(onDataLoaded = { showSplash = false })
        } else {
            val cardTheme by remember(appGraph) {
                combine(
                    appGraph.playerEconomyRepository.selectedTheme,
                    appGraph.playerEconomyRepository.selectedSkin,
                ) { theme, skin -> CardTheme(back = theme, skin = skin) }
            }.collectAsState(CardTheme())

            CompositionLocalProvider(
                LocalAppGraph provides appGraph,
                LocalCardTheme provides cardTheme,
            ) {
                Children(
                    stack = root.childStack,
                    animation =
                        predictiveBackAnimation(
                            backHandler = root.backHandler,
                            fallbackAnimation = stackAnimation(slide() + fade()),
                            onBack = root::pop,
                        ),
                ) {
                    when (val child = it.instance) {
                        is RootComponent.Child.Start -> StartContent(child.component)
                        is RootComponent.Child.Game -> GameContent(child.component)
                        is RootComponent.Child.Settings -> SettingsContent(child.component)
                        is RootComponent.Child.Stats -> StatsContent(child.component)
                        is RootComponent.Child.Shop -> ShopContent(child.component)
                        is RootComponent.Child.Debug -> DebugContent(child.component)
                    }
                }
            }
        }
    }
}
