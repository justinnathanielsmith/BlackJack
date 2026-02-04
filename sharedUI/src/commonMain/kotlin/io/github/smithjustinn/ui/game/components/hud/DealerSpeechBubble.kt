package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.text.font.FontFamily

@Composable
fun DealerSpeechBubble(
    matchComment: MatchComment?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = matchComment != null,
        enter = fadeIn(animationSpec = spring()) + scaleIn(initialScale = 0.8f),
        exit = fadeOut(animationSpec = spring()) + scaleOut(targetScale = 0.8f),
        modifier = modifier,
    ) {
        matchComment?.let {
            SpeechBubbleContent(it)
        }
    }
}

@Composable
private fun SpeechBubbleContent(matchComment: MatchComment) {
    @Suppress("SpreadOperator")
    val commentText = stringResource(matchComment.res, *matchComment.args.toTypedArray())

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Bubble Body
        Box(
            modifier =
                Modifier
                    .shadow(
                        elevation = PokerTheme.spacing.small,
                        shape = BubbleShape,
                        spotColor = Color.Black.copy(alpha = 0.5f),
                    ).background(Color.White, BubbleShape)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .widthIn(max = 280.dp),
        ) {
            Text(
                text = commentText,
                style =
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        color = Color.Black,
                    ),
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }

        // Bubble Tail (Triangle)
        Canvas(modifier = Modifier.padding(top = 0.dp).size(16.dp, 10.dp).padding(bottom = 0.dp)) {
            val width = size.width
            val height = size.height
            val path =
                Path().apply {
                    moveTo(0f, 0f)
                    lineTo(width / 2, height)
                    lineTo(width, 0f)
                    close()
                }
            drawPath(path, color = Color.White)
        }
    }
}

private val BubbleShape = RoundedCornerShape(16.dp)
