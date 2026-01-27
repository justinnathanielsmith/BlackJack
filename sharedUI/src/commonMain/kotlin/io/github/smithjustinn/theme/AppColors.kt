package io.github.smithjustinn.theme

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color

@Immutable
data class AppColors(
    val primary: Color,
    val onPrimary: Color,
    val background: Color,
    val onBackground: Color,
    val surface: Color,
    val onSurface: Color,
    val error: Color,
    val onError: Color,

    // Custom Gameplay Colors
    val tacticalRed: Color,
    val goldenYellow: Color,
    val bonusGreen: Color,
    val softBlue: Color,

    // Heat Mode Colors
    val heatBackgroundTop: Color,
    val heatBackgroundBottom: Color,

    // Refined Poker Theme Colors
    val feltGreen: Color,
    val feltGreenDark: Color,
    val oakWood: Color,
    val pillSelected: Color,
    val pillUnselected: Color,
    val hudBackground: Color,
)

internal val LightAppColors =
    AppColors(
        primary = GoldenYellow,
        onPrimary = Color.Black,
        background = Color(0xFF35654d),
        onBackground = Color.White,
        surface = Color(0xFF4E2C1C),
        onSurface = GoldenYellow,
        error = TacticalRed,
        onError = Color.White,

        tacticalRed = TacticalRed,
        goldenYellow = GoldenYellow,
        bonusGreen = BonusGreen,
        softBlue = SoftBlue,

        heatBackgroundTop = HeatBackgroundTop,
        heatBackgroundBottom = HeatBackgroundBottom,

        feltGreen = FeltGreenTop,
        feltGreenDark = FeltGreenBottom,
        oakWood = Color(0xFF4E2C1C),
        pillSelected = Color(0xFFF5F5DC),
        pillUnselected = Color(0xFF1B4D3E),
        hudBackground = Color(0xCC000000),
    )

internal val DarkAppColors = LightAppColors.copy(
    background = Color(0xFF1e3a2f),
    surface = Color(0xFF2D1409),
)
