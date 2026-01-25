package io.github.smithjustinn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.theme.AppTheme
import io.github.smithjustinn.ui.game.GameContent
import io.github.smithjustinn.ui.root.RootComponent
import io.github.smithjustinn.ui.settings.SettingsContent
import io.github.smithjustinn.ui.start.StartContent
import io.github.smithjustinn.ui.stats.StatsContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun App(root: RootComponent, appGraph: AppGraph, onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}) =
    AppTheme(onThemeChanged) {
        CompositionLocalProvider(LocalAppGraph provides appGraph) {
            Children(
                stack = root.childStack,
                animation =
                predictiveBackAnimation(
                    backHandler = root.backHandler,
                    fallbackAnimation = stackAnimation(slide() + fade()),
                    onBack = root::pop,
                ),
            ) {
                val child = it.instance
                when (child) {
                    is RootComponent.Child.Start -> StartContent(child.component)
                    is RootComponent.Child.Game -> GameContent(child.component)
                    is RootComponent.Child.Settings -> SettingsContent(child.component)
                    is RootComponent.Child.Stats -> StatsContent(child.component)
                }
            }
        }
    }
