package io.github.smithjustinn.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

private val LightColorScheme =
    lightColorScheme(
        primary = GoldenYellow,
        onPrimary = Color.Black,
        background = FeltGreenTop,
        onBackground = Color.White,
        surface = Color(0xFF4E2C1C), // Oak Woodish
        onSurface = GoldenYellow,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = GoldenYellow,
        onPrimary = Color.Black,
        background = FeltGreenBottom,
        onBackground = Color.White,
        surface = Color(0xFF2D1409), // Darker Oak
        onSurface = GoldenYellow,
    )

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }
internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }

object PokerTheme {
    val colors: AppColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current

    val spacing: AppSpacing
        @Composable
        @ReadOnlyComposable
        get() = LocalAppSpacing.current

    val shapes: AppShapes
        @Composable
        @ReadOnlyComposable
        get() = LocalAppShapes.current

    val typography: androidx.compose.material3.Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current
}

@Composable
fun AppTheme(
    onThemeChanged: @Composable (isDark: Boolean) -> Unit = {},
    content: @Composable () -> Unit,
) {
    val systemIsDark = isSystemInDarkTheme()
    val isDarkState = remember(systemIsDark) { mutableStateOf(systemIsDark) }
    val colors = if (isDarkState.value) DarkAppColors else LightAppColors
    val spacing = AppSpacing()
    val shapes = AppShapes()
    val typography = androidx.compose.material3.Typography()

    CompositionLocalProvider(
        LocalThemeIsDark provides isDarkState,
        LocalAppColors provides colors,
        LocalAppSpacing provides spacing,
        LocalAppShapes provides shapes,
        LocalAppTypography provides typography,
    ) {
        val isDark by isDarkState
        onThemeChanged(isDark)
        MaterialTheme(
            colorScheme = if (isDark) DarkColorScheme else LightColorScheme,
            typography = typography,
            content = { Surface(content = content) },
        )
    }
}
