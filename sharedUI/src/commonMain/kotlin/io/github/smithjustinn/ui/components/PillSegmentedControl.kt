package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme

@Composable
fun <T> PillSegmentedControl(
    items: List<T>,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String,
    modifier: Modifier = Modifier,
) {
    val selectedIndex = items.indexOf(selectedItem)
    val shape = RoundedCornerShape(CORNER_PERCENT)
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    BoxWithConstraints(
        modifier =
            modifier
                .height(48.dp)
                .clip(shape)
                .background(colors.pillUnselected)
                .padding(spacing.extraSmall),
    ) {
        val itemWidth = maxWidth / items.size

        val indicatorOffset by animateDpAsState(
            targetValue = itemWidth * selectedIndex,
            label = "indicatorOffset",
        )

        // Selected Indicator
        Box(
            modifier =
                Modifier
                    .width(itemWidth)
                    .fillMaxHeight()
                    .offset(x = indicatorOffset)
                    .clip(shape)
                    .background(colors.pillSelected),
        )

        // Labels
        Row(modifier = Modifier.fillMaxSize()) {
            items.forEach { item ->
                Box(
                    modifier =
                        Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clickable { onItemSelected(item) },
                    contentAlignment = Alignment.Center,
                ) {
                    val isSelected = item == selectedItem
                    Text(
                        text = labelProvider(item),
                        style = PokerTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) colors.feltGreenDark else Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

private const val CORNER_PERCENT = 50
