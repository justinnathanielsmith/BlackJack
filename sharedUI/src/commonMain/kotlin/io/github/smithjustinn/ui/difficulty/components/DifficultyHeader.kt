package io.github.smithjustinn.ui.difficulty.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

@Composable
fun DifficultyHeader(
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val title = stringResource(Res.string.app_name)
        val fontSize = if (scale < 1f) 36.sp else 52.sp
        
        // Outline Layer
        Text(
            text = title,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1E3A8A), // Dark Blue Outline
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                drawStyle = Stroke(width = 24f * scale, join = StrokeJoin.Round)
            ),
            maxLines = 2,
            softWrap = true
        )

        // Inner White Stroke
        Text(
            text = title,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                drawStyle = Stroke(width = 12f * scale, join = StrokeJoin.Round)
            ),
            maxLines = 2,
            softWrap = true
        )
        
        // Gradient Fill
        Text(
            text = title,
            style = TextStyle(
                fontSize = fontSize,
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFFFFFFF), // White
                        Color(0xFFBAE6FD), // Light Blue
                        Color(0xFF60A5FA)  // Blue
                    )
                ),
                textAlign = TextAlign.Center,
                letterSpacing = 2.sp,
                shadow = Shadow(
                    color = Color(0xFFFCD34D), // Gold Glow
                    offset = Offset(0f, 0f),
                    blurRadius = 30f * scale
                )
            ),
            maxLines = 2,
            softWrap = true
        )
    }
}
