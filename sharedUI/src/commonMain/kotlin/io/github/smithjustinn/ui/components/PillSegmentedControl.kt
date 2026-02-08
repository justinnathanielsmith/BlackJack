package io.github.smithjustinn.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.HapticFeedbackType
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

        SegmentIndicator(
            itemWidth = itemWidth,
            offset = indicatorOffset,
            shape = shape,
            color = colors.pillSelected,
        )

        SegmentLabels(
            items = items,
            selectedItem = selectedItem,
            itemWidth = itemWidth,
            onItemSelected = onItemSelected,
            labelProvider = labelProvider,
        )
    }
}

@Composable
private fun SegmentIndicator(
    itemWidth: androidx.compose.ui.unit.Dp,
    offset: androidx.compose.ui.unit.Dp,
    shape: RoundedCornerShape,
    color: Color,
) {
    Box(
        modifier =
            Modifier
                .width(itemWidth)
                .fillMaxHeight()
                .offset(x = offset)
                .clip(shape)
                .background(color),
    )
}

@Composable
private fun <T> SegmentLabels(
    items: List<T>,
    selectedItem: T,
    itemWidth: androidx.compose.ui.unit.Dp,
    onItemSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String,
) {
    val hapticsService = LocalAppGraph.current.hapticsService
    val colors = PokerTheme.colors

    Row(modifier = Modifier.fillMaxSize()) {
        items.forEach { item ->
            val isSelected = item == selectedItem
            Box(
                modifier =
                    Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .selectable(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
                                    onItemSelected(item)
                                }
                            },
                            role = Role.RadioButton,
                        ),
                contentAlignment = Alignment.Center,
            ) {
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

private const val CORNER_PERCENT = 50
