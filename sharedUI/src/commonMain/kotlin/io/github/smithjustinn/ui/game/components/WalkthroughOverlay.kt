package io.github.smithjustinn.ui.game.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.app_name
import io.github.smithjustinn.resources.walkthrough_desc_combos
import io.github.smithjustinn.resources.walkthrough_desc_find_pairs
import io.github.smithjustinn.resources.walkthrough_desc_welcome
import io.github.smithjustinn.resources.walkthrough_got_it
import io.github.smithjustinn.resources.walkthrough_next
import io.github.smithjustinn.resources.walkthrough_skip
import io.github.smithjustinn.resources.walkthrough_title_combos
import io.github.smithjustinn.resources.walkthrough_title_find_pairs
import io.github.smithjustinn.resources.walkthrough_title_welcome
import io.github.smithjustinn.theme.GoldenYellow
import io.github.smithjustinn.theme.InactiveBackground
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun WalkthroughOverlay(
    step: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f))
                // Consume clicks
                .clickable(enabled = false) {},
    ) {
        WalkthroughDialog(
            step = step,
            onNext = onNext,
            onDismiss = onDismiss,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
private fun WalkthroughDialog(
    step: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier =
            modifier
                .padding(32.dp)
                .widthIn(max = 400.dp),
        shape = RoundedCornerShape(24.dp),
        color = InactiveBackground.copy(alpha = 0.9f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        shadowElevation = 24.dp,
    ) {
        WalkthroughContent(step, onNext, onDismiss)
    }
}

@Composable
private fun WalkthroughContent(
    step: Int,
    onNext: () -> Unit,
    onDismiss: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(getTitleRes(step)).uppercase(),
            style =
                MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
            fontWeight = FontWeight.Black,
            color = GoldenYellow,
            textAlign = TextAlign.Center,
            letterSpacing = 1.sp,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(getDescriptionRes(step)),
            style =
                MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                ),
            textAlign = TextAlign.Center,
            color = Color.White.copy(alpha = 0.9f),
            lineHeight = 24.sp,
        )

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text(
                    stringResource(Res.string.walkthrough_skip),
                    color = Color.White.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                )
            }

            Button(
                onClick = {
                    if (step < 2) onNext() else onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldenYellow),
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    if (step <
                        2
                    ) {
                        stringResource(Res.string.walkthrough_next)
                    } else {
                        stringResource(Res.string.walkthrough_got_it)
                    },
                    fontWeight = FontWeight.Black,
                    color = Color.Black, // Dark text on Gold button
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                )
            }
        }
    }
}

private fun getTitleRes(step: Int): StringResource =
    when (step) {
        0 -> Res.string.walkthrough_title_welcome
        1 -> Res.string.walkthrough_title_find_pairs
        2 -> Res.string.walkthrough_title_combos
        else -> Res.string.app_name
    }

private fun getDescriptionRes(step: Int): StringResource =
    when (step) {
        0 -> Res.string.walkthrough_desc_welcome
        1 -> Res.string.walkthrough_desc_find_pairs
        2 -> Res.string.walkthrough_desc_combos
        else -> Res.string.app_name
    }
