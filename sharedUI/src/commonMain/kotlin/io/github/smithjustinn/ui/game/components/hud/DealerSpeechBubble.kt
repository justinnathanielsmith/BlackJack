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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.theme.PokerTheme
import org.jetbrains.compose.resources.stringResource

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

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier.clearAndSetSemantics {
                contentDescription = commentText
            },
    ) {
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
            TypewriterText(
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

@Composable
private fun TypewriterText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    fontWeight: FontWeight? = null,
    textAlign: TextAlign? = null,
) {
    var displayedText by remember(text) { mutableStateOf("") }

    LaunchedEffect(text) {
        displayedText = ""
        var currentIndex = 0
        while (currentIndex < text.length) {
            kotlinx.coroutines.delay(30)

            // Advance by length to avoid splitting surrogate pairs
            val charLength =
                if (text[currentIndex].isHighSurrogate() && currentIndex + 1 < text.length) {
                    2
                } else {
                    1
                }
            currentIndex += charLength

            displayedText = text.substring(0, currentIndex)
        }
    }

    Text(
        text = displayedText,
        modifier = modifier,
        style = style,
        fontWeight = fontWeight,
        textAlign = textAlign,
    )
}

private val BubbleShape = RoundedCornerShape(16.dp)
