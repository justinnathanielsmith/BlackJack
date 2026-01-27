package io.github.smithjustinn.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.ui.theme.PokerTheme

@Composable
fun DealerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PokerTheme.ChipGreen,
    textColor: Color = PokerTheme.Gold,
    size: Dp = 80.dp,
    enabled: Boolean = true,
) {
    if (enabled) {
        PokerChip(
            text = "", // We render text manually to control style better for buttons
            contentColor = color,
            isSelected = true,
            onClick = onClick,
            modifier = modifier,
            size = size,
        )
        // Overlay text on top of the generic chip content rendered by PokerChip
        // Wait, PokerChip renders text below. We want text INSIDE for Dealer Button usually.
        // Let's implement DealerButton independently using similar style but text inside.
    }
}

@Composable
fun LargeDealerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = PokerTheme.ChipGreen,
    textColor: Color = PokerTheme.Gold,
    size: Dp = 80.dp,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier,
    ) {
        PokerChip(
            text = "",
            contentColor = color,
            isSelected = true,
            onClick = onClick,
            size = size,
        )

        Text(
            text = text.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
