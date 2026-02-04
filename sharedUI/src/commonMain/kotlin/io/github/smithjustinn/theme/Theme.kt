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
import io.github.smithjustinn.domain.models.CardTheme
import androidx.compose.material3.Typography

private val LightColorScheme =
    lightColorScheme(
        primary = ModernGold,
        onPrimary = Color.Black,
        background = EmeraldGreenTop,
        onBackground = Color.White,
        surface = Color(0xFF4E2C1C), // Oak Woodish
        onSurface = ModernGold,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = ModernGold,
        onPrimary = Color.Black,
        background = EmeraldGreenDark,
        onBackground = Color.White,
        surface = Color(0xFF2D1409), // Darker Oak
        onSurface = ModernGold,
    )

internal val LocalThemeIsDark = compositionLocalOf { mutableStateOf(true) }
internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }
internal val LocalCardTheme = staticCompositionLocalOf { CardTheme() }

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

    val typography: Typography
        @Composable
        @ReadOnlyComposable
        get() = LocalAppTypography.current

    val cardTheme: CardTheme
        @Composable
        @ReadOnlyComposable
        get() = LocalCardTheme.current
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
    val typography = Typography()

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
