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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.CardTheme
import io.github.smithjustinn.theme.AppTheme
import io.github.smithjustinn.theme.LocalCardTheme
import io.github.smithjustinn.ui.root.RootComponent
import io.github.smithjustinn.ui.root.RootContent
import io.github.smithjustinn.ui.splash.SplashScreen
import kotlinx.coroutines.flow.combine

private const val SPLASH_ANIMATION_DURATION = 1000

@Composable
fun App(
    root: RootComponent,
    appGraph: AppGraph,
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
) = AppTheme(onThemeChanged) {
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
        var showSplash by rememberSaveable { mutableStateOf(true) }

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
                RootContent(component = root)
            }
        }
    }
}
