package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.theme.PokerTheme

private const val GLASS_STROKE_WIDTH_DP = 1f

@Composable
fun GlassDashboard(
    modifier: Modifier = Modifier,
    isHeatMode: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colors = PokerTheme.colors
    val spacing = PokerTheme.spacing

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(vertical = spacing.small),
    ) {
        // Glass Background with Border
        Canvas(modifier = Modifier.matchParentSize()) {
            val strokeWidth = GLASS_STROKE_WIDTH_DP.dp.toPx()

            // 1. Semi-transparent dark background
            drawRect(
                color = colors.hudBackground,
                size = size,
            )

            // 2. Glass Border Gradient (Top-Left White -> Bottom-Right Transparent)
            drawRect(
                brush =
                    Brush.linearGradient(
                        colors =
                            listOf(
                                // Highlight (boosted slightly from base 10% for visibility)
                                colors.glassWhite.copy(alpha = 0.4f),
                                colors.glassWhite.copy(alpha = 0.05f), // Shadow
                            ),
                        start = Offset(0f, 0f),
                        end = Offset(size.width, size.height),
                    ),
                style = Stroke(width = strokeWidth),
            )

            // 3. Heat Mode Glow Overlay
            if (isHeatMode) {
                drawRect(
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    colors.tacticalRed.copy(alpha = 0.2f),
                                    Color.Transparent,
                                ),
                        ),
                )
            }
        }

        // Content Container
        Box(modifier = Modifier.padding(spacing.small)) {
            content()
        }
    }
}
