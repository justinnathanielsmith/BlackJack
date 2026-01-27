package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PeekCountdownOverlay(
    countdown: Int,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = countdown,
            transitionSpec = {
                (scaleIn() + fadeIn()).togetherWith(scaleOut() + fadeOut())
            },
        ) { targetCount ->
            Surface(
                modifier = Modifier.size(140.dp),
                shape = CircleShape,
                color =
                    io.github.smithjustinn.theme.MemoryMatchTheme.colors.inactiveBackground
                        .copy(alpha = 0.9f),
                border = BorderStroke(4.dp, io.github.smithjustinn.theme.MemoryMatchTheme.colors.goldenYellow),
                shadowElevation = 16.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = targetCount.toString(),
                        style =
                            MaterialTheme.typography.displayLarge.copy(
                                fontSize = 80.sp,
                                fontWeight = FontWeight.Black,
                                color = io.github.smithjustinn.theme.MemoryMatchTheme.colors.goldenYellow,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                            ),
                    )
                }
            }
        }
    }
}
