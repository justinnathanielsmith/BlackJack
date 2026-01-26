package io.github.smithjustinn.ui.game.components

import androidx.compose.ui.graphics.Color

data class TimerVisuals(
    val color: Color,
    val scale: Float,
    val layout: TimerLayout = TimerLayout.STANDARD,
)
