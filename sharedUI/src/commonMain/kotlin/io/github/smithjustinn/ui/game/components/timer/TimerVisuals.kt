package io.github.smithjustinn.ui.game.components.timer

import androidx.compose.runtime.State
import androidx.compose.ui.graphics.Color

data class TimerVisuals(
    val color: Color,
    val scale: State<Float>,
    val layout: TimerLayout = TimerLayout.STANDARD,
)
