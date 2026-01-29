package io.github.smithjustinn.ui.game.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.time.Duration
import kotlin.time.TimeMark

data class FlyingPoint(
    val id: Long,
    val startPos: Offset,
    val targetPos: Offset,
    val startTime: TimeMark,
    val duration: Duration,
    val color: Color,
    val size: Float,
    val delay: Duration,
)
