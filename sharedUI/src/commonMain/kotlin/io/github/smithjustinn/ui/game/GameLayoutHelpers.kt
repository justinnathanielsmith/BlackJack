package io.github.smithjustinn.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.game.components.effects.CardFountainOverlay
import io.github.smithjustinn.ui.game.components.effects.ConfettiEffect
import io.github.smithjustinn.ui.game.components.hud.NewHighScoreSnackbar
import io.github.smithjustinn.ui.game.components.hud.ResultsCard
import kotlinx.coroutines.launch

private const val HEAT_TRANSITION_DURATION_MS = 800
private const val BLACK_OVERLAY_ALPHA = 0.4f
private const val SNACKBAR_TOP_PADDING = 16
private const val SNACKBAR_HORIZONTAL_PADDING = 16
private const val SNACKBAR_MAX_WIDTH = 500
private const val RESULTS_MAX_WIDTH = 550

@Composable
fun GameBackground(
    isHeatMode: Boolean,
    theme: CardBackTheme,
) {
    val colors = PokerTheme.colors

    val themeTopColor =
        when (theme) {
            CardBackTheme.GEOMETRIC -> Color(0xFF121212)
            CardBackTheme.CLASSIC -> colors.feltGreen
            CardBackTheme.PATTERN -> Color(0xFF16212E)
            CardBackTheme.POKER -> Color(0xFF231515)
        }

    val themeBottomColor =
        when (theme) {
            CardBackTheme.GEOMETRIC -> Color(0xFF1E1E1E)
            CardBackTheme.CLASSIC -> colors.feltGreenDark
            CardBackTheme.PATTERN -> Color(0xFF243447)
            CardBackTheme.POKER -> Color(0xFF352020)
        }

    val backgroundTopColor by animateColorAsState(
        targetValue = if (isHeatMode) colors.heatBackgroundTop else themeTopColor,
        animationSpec = tween(durationMillis = HEAT_TRANSITION_DURATION_MS),
        label = "backgroundTop",
    )
    val backgroundBottomColor by animateColorAsState(
        targetValue = if (isHeatMode) colors.heatBackgroundBottom else themeBottomColor,
        animationSpec = tween(durationMillis = HEAT_TRANSITION_DURATION_MS),
        label = "backgroundBottom",
    )

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(backgroundTopColor, backgroundBottomColor),
                    ),
                ),
    )
}

@Composable
fun GameGameOverOverlay(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = BLACK_OVERLAY_ALPHA)),
    ) {
        // Main content
        GameResultsOverlay(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
        )

        if (state.game.isGameWon) {
            // Celebration effects - now above the results card
            CardFountainOverlay(
                cards = state.game.cards,
                cardBackTheme = state.cardBackTheme,
                cardSymbolTheme = state.cardSymbolTheme,
                areSuitsMultiColored = state.areSuitsMultiColored,
            )

            ConfettiEffect()

            if (state.isNewHighScore) {
                NewHighScoreSnackbar(
                    modifier =
                        Modifier
                            .align(Alignment.TopCenter)
                            .padding(
                                top = SNACKBAR_TOP_PADDING.dp,
                                start = SNACKBAR_HORIZONTAL_PADDING.dp,
                                end = SNACKBAR_HORIZONTAL_PADDING.dp,
                            ).widthIn(max = SNACKBAR_MAX_WIDTH.dp),
                )
            }
        }
    }
}

@Composable
private fun BoxScope.GameResultsOverlay(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val hapticsService = graph.hapticsService
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    ResultsCard(
        isWon = state.game.isGameWon,
        isBusted = state.game.isBusted,
        highScore = maxOf(state.game.score, state.bestScore),
        moves = state.game.moves,
        elapsedTimeSeconds = state.elapsedTimeSeconds,
        scoreBreakdown = state.game.scoreBreakdown,
        onPlayAgain = {
            audioService.playEffect(AudioService.SoundEffect.CLICK)
            component.onRestart()
            audioService.startMusic()
        },
        onShareReplay = {
            audioService.playEffect(AudioService.SoundEffect.CLICK)
            val seed = state.game.seed ?: 0L
            val link = "memorymatch://game?mode=${state.game.mode}&pairs=${state.game.pairCount}&seed=$seed"
            scope.launch {
                clipboardManager.setText(AnnotatedString(link))
            }
            hapticsService.vibrateMatch()
        },
        onScoreTick = { hapticsService.vibrateTick() },
        modifier =
            Modifier
                .align(Alignment.Center)
                .widthIn(max = RESULTS_MAX_WIDTH.dp)
                .padding(
                    vertical = if (useCompactUI) PokerTheme.spacing.small else PokerTheme.spacing.large,
                ),
        mode = state.game.mode,
    )
}

@Composable
fun HeatModeTransitionHandler(
    isHeatMode: Boolean,
    onHeatLost: () -> Unit,
) {
    val currentOnHeatLost by rememberUpdatedState(onHeatLost)
    var lastHeatMode by remember { mutableStateOf(isHeatMode) }
    LaunchedEffect(isHeatMode) {
        if (lastHeatMode && !isHeatMode) {
            currentOnHeatLost()
        }
        lastHeatMode = isHeatMode
    }
}

private const val SHAKE_MAX_OFFSET = 20f
private const val SHAKE_MID_OFFSET = 10f
private const val SHAKE_RESET_OFFSET = 0f

suspend fun runShakeAnimation(animatable: Animatable<Float, *>) {
    animatable.animateTo(SHAKE_MAX_OFFSET, spring(stiffness = Spring.StiffnessHigh))
    animatable.animateTo(-SHAKE_MAX_OFFSET, spring(stiffness = Spring.StiffnessHigh))
    animatable.animateTo(SHAKE_MID_OFFSET, spring(stiffness = Spring.StiffnessHigh))
    animatable.animateTo(SHAKE_RESET_OFFSET, spring(stiffness = Spring.StiffnessMedium))
}
