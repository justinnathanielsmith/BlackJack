package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.theme.PokerTheme

@Composable
fun PokerChip(
    text: String,
    contentColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 64.dp,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val elevation by animateDpAsState(
        targetValue = if (isPressed) PokerTheme.spacing.extraSmall else if (isSelected) PokerTheme.spacing.small else PokerTheme.spacing.extraSmall,
        label = "chipElevation"
    )
    
    val scale = if (isSelected) 1.1f else 1.0f

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(size * scale)
                .shadow(elevation, CircleShape)
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = onClick
                )
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val radius = this.size.minDimension / 2
                val center = Offset(this.size.width / 2, this.size.height / 2)

                // 1. Base Chip Color
                drawCircle(
                    color = contentColor,
                    center = center,
                    radius = radius
                )

                // 2. Dashed Ring (White spots)
                drawCircle(
                    color = Color.White,
                    center = center,
                    radius = radius * 0.85f,
                    style = Stroke(
                        width = radius * 0.15f,
                        pathEffect = PathEffect.dashPathEffect(
                            intervals = floatArrayOf(20f, 20f),
                            phase = 0f
                        )
                    )
                )

                // 3. Inner Circle border
                drawCircle(
                    color = Color.White.copy(alpha = 0.8f),
                    center = center,
                    radius = radius * 0.6f,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = text,
            style = PokerTheme.typography.labelMedium,
            color = if (isSelected) PokerTheme.colors.goldenYellow else Color.White.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = if (isSelected) 14.sp else 12.sp
        )
    }
}
