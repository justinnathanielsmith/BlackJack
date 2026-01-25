package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan

@Composable
fun PeekCountdownOverlay(countdown: Int, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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
                color = InactiveBackground.copy(alpha = 0.8f),
                border = BorderStroke(2.dp, NeonCyan.copy(alpha = 0.5f)),
                shadowElevation = 16.dp,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = targetCount.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 80.sp,
                            fontWeight = FontWeight.Black,
                            color = NeonCyan,
                        ),
                    )
                }
            }
        }
    }
}
