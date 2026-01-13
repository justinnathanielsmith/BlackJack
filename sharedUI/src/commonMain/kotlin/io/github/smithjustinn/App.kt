package io.github.smithjustinn

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.utils.BackPressScreen
import io.github.smithjustinn.ui.difficulty.DifficultyScreen
import io.github.smithjustinn.theme.AppTheme

@Composable
fun App(
    appGraph: AppGraph,
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {}
) = AppTheme(onThemeChanged) {
    CompositionLocalProvider(LocalAppGraph provides appGraph) {
        Navigator(
            screen = DifficultyScreen(),
            onBackPressed = { currentScreen ->
                if (currentScreen is BackPressScreen) {
                    currentScreen.handleBack()
                } else {
                    true
                }
            }
        ) { navigator ->
            SlideTransition(navigator)
        }
    }
}
