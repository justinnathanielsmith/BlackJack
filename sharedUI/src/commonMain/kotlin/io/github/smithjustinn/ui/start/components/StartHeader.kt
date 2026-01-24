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
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.theme.LightPurple
import io.github.smithjustinn.theme.SoftBlue
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import org.jetbrains.compose.resources.stringResource

/**
 * StartHeader
 */
@Composable
fun StartHeader(
    modifier: Modifier = Modifier,
    cardBackTheme: CardBackTheme = CardBackTheme.GEOMETRIC,
    cardSymbolTheme: CardSymbolTheme = CardSymbolTheme.CLASSIC,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        val title = stringResource(Res.string.app_name)

        Text(
            text = title,
            style = TextStyle(
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        LightPurple,
                        SoftBlue,
                    ),
                ),
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = LightPurple.copy(alpha = 0.4f),
                    offset = Offset(0f, 0f),
                    blurRadius = 30f,
                ),
            ),
        )

        Spacer(modifier = Modifier.height(32.dp))

        CardPreview(
            modifier = Modifier.height(180.dp),
            cardBackTheme = cardBackTheme,
            cardSymbolTheme = cardSymbolTheme,
        )
    }
}
