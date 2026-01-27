package io.github.smithjustinn.theme

import androidx.compose.ui.unit.dp

data class AppSpacing(
    val none: androidx.compose.ui.unit.Dp = 0.dp,
    val extraSmall: androidx.compose.ui.unit.Dp = 4.dp,
    val small: androidx.compose.ui.unit.Dp = 8.dp,
    val medium: androidx.compose.ui.unit.Dp = 16.dp,
    val large: androidx.compose.ui.unit.Dp = 24.dp,
    val extraLarge: androidx.compose.ui.unit.Dp = 32.dp,
    val huge: androidx.compose.ui.unit.Dp = 48.dp,
    val massive: androidx.compose.ui.unit.Dp = 64.dp,
)

val LocalAppSpacing = androidx.compose.runtime.staticCompositionLocalOf { AppSpacing() }
