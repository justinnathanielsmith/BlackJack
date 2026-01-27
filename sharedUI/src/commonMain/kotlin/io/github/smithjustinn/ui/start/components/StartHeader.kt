package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.app_name
import org.jetbrains.compose.resources.stringResource

/**
 * StartHeader
 */
@Composable
fun StartHeader(
    modifier: Modifier = Modifier,
    settings: CardDisplaySettings = CardDisplaySettings(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val title = stringResource(Res.string.app_name)

        Text(
            text = title,
            style =
                TextStyle(
                    fontSize = 48.sp,
                    fontWeight = FontWeight.ExtraBold,
                    brush =
                        Brush.verticalGradient(
                            colors =
                                listOf(
                                    io.github.smithjustinn.theme.GoldenYellow, // Existing gold color
                                    androidx.compose.ui.graphics
                                        .Color(0xFFB8860B), // DarkGoldenrod for depth
                                ),
                        ),
                    textAlign = TextAlign.Center,
                    shadow =
                        Shadow(
                            color =
                                androidx.compose.ui.graphics.Color.Black
                                    .copy(alpha = 0.6f),
                            offset = Offset(3f, 3f), // Sharp offset for physical depth
                            blurRadius = 2f, // Reduced blur to remove "glow"
                        ),
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
        )

        Spacer(modifier = Modifier.height(32.dp))

        CardPreview(
            modifier = Modifier.height(180.dp),
            settings = settings,
        )
    }
}
