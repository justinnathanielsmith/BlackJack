package io.github.smithjustinn.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan

/**
 * NeonSegmentedControl (2026 Design)
 * 
 * A custom segmented control with a rounded rectangle shape, animated sliding indicator,
 * and neon glow effects. Updated to match the refined aesthetic.
 */
@Composable
fun <T> NeonSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String,
    modifier: Modifier = Modifier
) {
    val selectedIndex = items.indexOf(selectedItem).coerceAtLeast(0)
    val controlShape = RoundedCornerShape(12.dp)
    
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(controlShape)
            .background(InactiveBackground.copy(alpha = 0.5f))
            .padding(4.dp)
    ) {
        val maxWidth = maxWidth
        val itemWidth = maxWidth / items.size
        
        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            animationSpec = tween(durationMillis = 300),
            label = "indicatorOffset"
        )

        // Sliding Indicator (The "active" state background)
        Box(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .width(itemWidth)
                .fillMaxHeight()
                .shadow(
                    elevation = 8.dp,
                    shape = controlShape,
                    ambientColor = NeonCyan.copy(alpha = 0.5f),
                    spotColor = NeonCyan.copy(alpha = 0.5f)
                )
                .clip(controlShape)
                .background(NeonCyan)
        )

        // Labels Row
        Row(modifier = Modifier.fillMaxSize()) {
            items.forEachIndexed { index, item ->
                val isSelected = index == selectedIndex
                val textColor by animateColorAsState(
                    targetValue = if (isSelected) Color.White else Color.White.copy(alpha = 0.5f),
                    animationSpec = tween(durationMillis = 300),
                    label = "textColor"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(controlShape)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onItemSelected(item) }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = labelProvider(item),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = textColor
                    )
                }
            }
        }
    }
}
