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
    val feltGreenCenter: Color,
    val feltGreenTop: Color,
    val oakWood: Color,
    val pillSelected: Color,
    val pillUnselected: Color,
    val hudBackground: Color,
    val brass: Color,
    val silver: Color,
    val bronze: Color,
    val tableShadow: Color,
    val glassWhite: Color,
)

internal val LightAppColors =
    AppColors(
        primary = ModernGold,
        onPrimary = Color.Black,
        background = EmeraldGreen,
        onBackground = Color.White,
        surface = CasinoBlack,
        onSurface = ModernGold,
        error = TacticalRed,
        onError = Color.White,
        tacticalRed = TacticalRed,
        goldenYellow = ModernGold,
        bonusGreen = BonusGreen,
        softBlue = SoftBlue,
        heatBackgroundTop = HeatBackgroundTop,
        heatBackgroundBottom = HeatBackgroundBottom,
        feltGreen = EmeraldGreen,
        feltGreenDark = EmeraldGreenDark,
        feltGreenCenter = EmeraldGreenCenter,
        feltGreenTop = EmeraldGreenTop,
        oakWood = CasinoBlack,
        pillSelected = ModernGold,
        pillUnselected = GlassBlack,
        hudBackground = GlassBlack,
        brass = Brass,
        silver = Silver,
        bronze = Bronze,
        tableShadow = TableShadow,
        glassWhite = GlassWhite,
    )

internal val DarkAppColors =
    LightAppColors.copy(
        background = DeepFeltGreen,
        feltGreen = DeepFeltGreen,
        feltGreenDark = DeepFeltGreenDark,
        feltGreenCenter = EmeraldGreen, // Still somewhat vibrant in center
        feltGreenTop = DeepFeltGreen,
        surface = Color(0xFF0A0503), // Almost black but warm
    )

internal val HeatAppColors =
    DarkAppColors.copy(
        primary = NeonCyan,
        onPrimary = Color.Black,
        background = HeatBackgroundTop,
        goldenYellow = NeonYellow,
        softBlue = NeonCyan,
        bonusGreen = NeonMagenta,
        feltGreen = HeatFeltRed,
        feltGreenDark = HeatFeltRedDark,
        feltGreenCenter = HeatFeltRedCenter,
        feltGreenTop = HeatFeltRedTop,
        pillSelected = NeonCyan,
        onSurface = NeonCyan,
    )
