package io.github.smithjustinn.ui.splash

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.CardDisplaySettings
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.splash_dealing_cards
import io.github.smithjustinn.resources.splash_title
import io.github.smithjustinn.ui.components.GlimmerText
import io.github.smithjustinn.ui.components.pokerBackground
import io.github.smithjustinn.ui.components.rememberGlimmerBrush
import io.github.smithjustinn.ui.start.components.CardPreview
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.stringResource

private const val SPLASH_DELAY_MS = 2500L
private const val SPLASH_FADE_DURATION_MS = 1200

@Composable
fun SplashScreen(onDataLoaded: () -> Unit) {
    var isVisible by remember { mutableStateOf(false) }

    // Fix for ktlint(compose:lambda-param-in-effect)
    val currentOnDataLoaded by rememberUpdatedState(onDataLoaded)

    LaunchedEffect(Unit) {
        isVisible = true
        delay(SPLASH_DELAY_MS) // Slightly longer to appreciate the "premium" feel
        currentOnDataLoaded()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .pokerBackground(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(SPLASH_FADE_DURATION_MS)),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                // Main Title with Golden Glimmer
                GlimmerText(stringResource(Res.string.splash_title), fontSize = 36.sp)

                Spacer(modifier = Modifier.height(32.dp))

                // Animated Card Preview
                CardPreview(
                    settings = CardDisplaySettings(),
                    modifier = Modifier.width(280.dp), // Slightly smaller to look focused
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Subtle Loading Text
                val glimmerBrush = rememberGlimmerBrush()
                Text(
                    text = stringResource(Res.string.splash_dealing_cards),
                    style =
                        MaterialTheme.typography.bodyMedium.copy(
                            brush = glimmerBrush,
                            fontWeight = FontWeight.Medium,
                        ),
                )
            }
        }
    }
}
