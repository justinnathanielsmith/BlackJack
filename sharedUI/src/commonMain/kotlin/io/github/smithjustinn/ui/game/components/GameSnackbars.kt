package io.github.smithjustinn.ui.game.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.new_high_score
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.ui.components.AppIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun NewHighScoreSnackbar(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "HighScorePulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec =
            infiniteRepeatable(
                animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse,
            ),
        label = "Scale",
    )

    Card(
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.9f), // Dark background
                contentColor = Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
        modifier =
            modifier
                .scale(scale)
                .border(
                    width = 2.dp,
                    brush =
                        Brush.linearGradient(
                            listOf(GoldenYellow, GoldenYellow.copy(alpha = 0.5f), GoldenYellow),
                        ),
                    shape = RoundedCornerShape(12.dp),
                ),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = AppIcons.Trophy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = GoldenYellow,
            )
            Text(
                text = stringResource(Res.string.new_high_score).uppercase(),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Icon(
                imageVector = AppIcons.Trophy,
                contentDescription = null,
                modifier = Modifier.size(32.dp),
                tint = GoldenYellow,
            )
        }
    }
}

@Composable
fun MatchCommentSnackbar(
    matchComment: MatchComment?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = matchComment != null,
        enter =
            fadeIn(animationSpec = spring()) +
                slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = spring(),
                ),
        exit =
            fadeOut(animationSpec = spring()) +
                slideOutVertically(
                    targetOffsetY = { it },
                    animationSpec = spring(),
                ),
        modifier = modifier,
    ) {
        matchComment?.let {
            MatchCommentCard(it)
        }
    }
}

@Composable
private fun MatchCommentCard(matchComment: MatchComment) {
    val commentText = stringResource(matchComment.res, *matchComment.args.toTypedArray())

    Card(
        shape = RoundedCornerShape(12.dp),
        colors =
            CardDefaults.cardColors(
                containerColor = Color.Black.copy(alpha = 0.8f),
                contentColor = Color.White,
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier =
            Modifier
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = GoldenYellow.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(12.dp),
                ),
    ) {
        Row(
            modifier =
                Modifier
                    .padding(horizontal = 20.dp, vertical = 14.dp)
                    .align(Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = "“",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                color = GoldenYellow,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = commentText,
                style =
                    MaterialTheme.typography.titleMedium.copy(
                        fontStyle = FontStyle.Italic,
                        lineHeight = 20.sp,
                        color = Color.White,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            Text(
                text = "”",
                style =
                    MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                color = GoldenYellow,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}
