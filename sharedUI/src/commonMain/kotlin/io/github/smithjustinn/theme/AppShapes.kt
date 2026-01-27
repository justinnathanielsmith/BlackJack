package io.github.smithjustinn.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

data class AppShapes(
    val small: Shape = RoundedCornerShape(4.dp),
    val medium: Shape = RoundedCornerShape(12.dp),
    val large: Shape = RoundedCornerShape(24.dp),
    val extraLarge: Shape = RoundedCornerShape(32.dp),
)

val LocalAppShapes = androidx.compose.runtime.staticCompositionLocalOf { AppShapes() }
