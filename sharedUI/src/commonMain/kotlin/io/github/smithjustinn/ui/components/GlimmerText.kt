package io.github.smithjustinn.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private const val TEXT_SHADOW_OFFSET_PX = 2f
private const val TEXT_SHADOW_BLUR_RADIUS = 4f

@Composable
fun GlimmerText(
    text: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 40.sp,
) {
    val brush = rememberGlimmerBrush()

    // Shadow Layer
    Box(modifier = modifier) {
        Text(
            text = text,
            style =
                TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    color = Color.Black.copy(alpha = 0.5f),
                ),
            modifier = Modifier.offset(x = 2.dp, y = 2.dp).clearAndSetSemantics { },
        )
        // Main Glimmer Layer
        Text(
            text = text,
            style =
                TextStyle(
                    fontSize = fontSize,
                    fontWeight = FontWeight.Black,
                    brush = brush,
                    shadow =
                        Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = Offset(TEXT_SHADOW_OFFSET_PX, TEXT_SHADOW_OFFSET_PX),
                            blurRadius = TEXT_SHADOW_BLUR_RADIUS,
                        ),
                ),
        )
    }
}
