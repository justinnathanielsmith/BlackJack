package io.github.smithjustinn.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DailyChallengeMutator
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.game_double_or_nothing
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.HeatAppColors
import io.github.smithjustinn.theme.LocalAppColors
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AdaptiveDensity
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.game.components.effects.ParticleEmbers
import io.github.smithjustinn.ui.game.components.effects.SteamEffect
import io.github.smithjustinn.ui.game.components.grid.GameGrid
import io.github.smithjustinn.ui.game.components.grid.GridCardState
import io.github.smithjustinn.ui.game.components.grid.GridSettings
import io.github.smithjustinn.ui.game.components.hud.ComboBadge
import io.github.smithjustinn.ui.game.components.hud.ComboBadgeState
import io.github.smithjustinn.ui.game.components.hud.DealerSpeechBubble
import io.github.smithjustinn.ui.game.components.hud.GameTopBar
import io.github.smithjustinn.ui.game.components.hud.GameTopBarState
import io.github.smithjustinn.ui.game.components.hud.MutatorIndicators
import io.github.smithjustinn.ui.game.components.overlays.PeekCountdownOverlay
import io.github.smithjustinn.ui.game.components.overlays.WalkthroughOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private object LayoutConstants {
    const val SHAKE_RESET_OFFSET = 0f
    const val STEAM_DURATION_MS = 1200
    const val COMPACT_HEIGHT_THRESHOLD_DP = 500
    const val DOUBLE_DOWN_BOTTOM_PADDING_DP = 100
    const val SPEECH_BUBBLE_TOP_PADDING_DP = 80
    const val MUTATOR_INDICATORS_TOP_OFFSET_DP = 60
    const val DOUBLE_DOWN_END_PADDING_DP = 16
    const val DEALER_SPEECH_MAX_WIDTH_DP = 600
}

private data class GameHUDState(
    val comboMultiplier: Int,
    val isMegaBonus: Boolean,
    val isHeatMode: Boolean,
    val activeMutators: Set<DailyChallengeMutator>,
    val isDoubleDownAvailable: Boolean,
    val matchComment: MatchComment?,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(
    component: GameComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(LayoutConstants.SHAKE_RESET_OFFSET) }
    var showSteam by remember { mutableStateOf(false) }

    GameEventHandler(
        component = component,
        onTheNuts = {
            showSteam = true
            scope.launch {
                runShakeAnimation(shakeOffset)
            }
            scope.launch {
                delay(LayoutConstants.STEAM_DURATION_MS.toLong())
                showSteam = false
            }
        },
    )

    AdaptiveDensity {
        HeatModeTransitionHandler(
            isHeatMode = state.isHeatMode,
            onHeatLost = {
                showSteam = true
                scope.launch { runShakeAnimation(shakeOffset) }
                scope.launch {
                    delay(LayoutConstants.STEAM_DURATION_MS.toLong())
                    showSteam = false
                }
            },
        )

        // Bolt: Pass shakeOffset object to defer read to draw phase
        GameMainScreenWrapper(
            state = state,
            component = component,
            shakeOffset = shakeOffset,
            showSteam = showSteam,
            modifier = modifier,
        )
    }
}

// Bolt: Pass Animatable instead of Float to read .value inside graphicsLayer, avoiding composition during animation
@Composable
private fun GameMainScreenWrapper(
    state: GameUIState,
    component: GameComponent,
    shakeOffset: Animatable<Float, AnimationVector1D>,
    showSteam: Boolean,
    modifier: Modifier = Modifier,
) {
    val currentColors = PokerTheme.colors
    val targetColors = if (state.isHeatMode) HeatAppColors else currentColors

    CompositionLocalProvider(LocalAppColors provides targetColors) {
        BoxWithConstraints(
            modifier =
                modifier
                    .fillMaxSize()
                    .graphicsLayer { translationX = shakeOffset.value },
        ) {
            val isLandscape = maxWidth > maxHeight
            val isCompactHeight = maxHeight < LayoutConstants.COMPACT_HEIGHT_THRESHOLD_DP.dp
            val useCompactUI = isLandscape && isCompactHeight

            Box(modifier = Modifier.fillMaxSize()) {
                GameBackground(
                    isHeatMode = state.isHeatMode,
                    theme = state.cardTheme,
                    comboMultiplier = state.game.comboMultiplier,
                )
                GameMainScreen(
                    state = state,
                    component = component,
                    useCompactUI = useCompactUI,
                )

                // Embers overlay - Foreground
                ParticleEmbers(isHeatMode = state.isHeatMode)

                // Steam Cool Down Effect
                SteamEffect(isVisible = showSteam)
            }
        }
    }
}

@Composable
private fun GameMainScreen(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    var scorePosition by remember { mutableStateOf(Offset.Zero) }
    // Bolt: Stabilize lambda to prevent unnecessary recompositions in GameTopBar
    val onScorePositioned = remember { { pos: Offset -> scorePosition = pos } }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GameTopBarContent(state, component, useCompactUI, onScorePositioned)
        },
    ) { paddingValues ->
        GameMainContent(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
            scorePosition = scorePosition,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        )
    }
}

@Composable
private fun GameTopBarContent(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
    onScorePositioned: (Offset) -> Unit,
) {
    val graph = LocalAppGraph.current
    val audioService = graph.audioService

    // Hoist event handlers to keep the call site clean
    val onBackClick =
        remember(component, audioService) {
            {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onBack()
            }
        }

    val onRestartClick =
        remember(component, audioService) {
            {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onRestart()
                audioService.startMusic()
            }
        }

    val onMuteClick =
        remember(component, audioService) {
            {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onToggleAudio()
            }
        }

    val gameTopBarState =
        remember(
            state.elapsedTimeSeconds,
            state.game.mode,
            state.maxTimeSeconds,
            state.showTimeGain,
            state.timeGainAmount,
            state.showTimeLoss,
            state.timeLossAmount,
            state.isMegaBonus,
            useCompactUI,
            state.isMusicEnabled,
            state.isSoundEnabled,
            state.game.score,
            state.game.currentPot,
            state.isHeatMode,
        ) {
            GameTopBarState(
                time = state.elapsedTimeSeconds,
                mode = state.game.mode,
                maxTime = state.maxTimeSeconds,
                showTimeGain = state.showTimeGain,
                timeGainAmount = state.timeGainAmount,
                showTimeLoss = state.showTimeLoss,
                timeLossAmount = state.timeLossAmount,
                isMegaBonus = state.isMegaBonus,
                compact = useCompactUI,
                isAudioEnabled = state.isMusicEnabled || state.isSoundEnabled,
                isLowTime =
                    state.game.mode == GameMode.TIME_ATTACK &&
                        state.elapsedTimeSeconds <= GameTopBarState.LOW_TIME_THRESHOLD_SEC,
                isCriticalTime =
                    state.game.mode == GameMode.TIME_ATTACK &&
                        state.elapsedTimeSeconds <= GameTopBarState.CRITICAL_TIME_THRESHOLD_SEC,
                bankedScore = state.game.score,
                currentPot = state.game.currentPot,
                isHeatMode = state.isHeatMode,
            )
        }

    GameTopBar(
        state = gameTopBarState,
        onBackClick = onBackClick,
        onRestartClick = onRestartClick,
        onMuteClick = onMuteClick,
        onScorePositioned = onScorePositioned,
    )
}

@Composable
private fun rememberGridCardState(state: GameUIState) =
    remember(state.game.cards, state.game.lastMatchedIds, state.isPeeking) {
        GridCardState(
            cards = state.game.cards,
            lastMatchedIds = state.game.lastMatchedIds,
            isPeeking = state.isPeeking,
        )
    }

@Composable
private fun rememberGridSettings(state: GameUIState) =
    remember(state.cardTheme, state.areSuitsMultiColored, state.isThirdEyeEnabled, state.showComboExplosion) {
        GridSettings(
            cardTheme = state.cardTheme,
            areSuitsMultiColored = state.areSuitsMultiColored,
            isThirdEyeEnabled = state.isThirdEyeEnabled,
            showComboExplosion = state.showComboExplosion,
        )
    }

@Composable
private fun rememberGameHUDState(state: GameUIState) =
    remember(
        state.game.comboMultiplier,
        state.isMegaBonus,
        state.isHeatMode,
        state.game.activeMutators,
        state.isDoubleDownAvailable,
        state.game.matchComment,
    ) {
        GameHUDState(
            comboMultiplier = state.game.comboMultiplier,
            isMegaBonus = state.isMegaBonus,
            isHeatMode = state.isHeatMode,
            activeMutators = state.game.activeMutators,
            isDoubleDownAvailable = state.isDoubleDownAvailable,
            matchComment = state.game.matchComment,
        )
    }

@Composable
private fun GameMainContent(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
    scorePosition: Offset,
    modifier: Modifier = Modifier,
) {
    val onCardClick =
        remember(component) {
            { cardId: Int ->
                component.onFlipCard(cardId)
            }
        }
    val onDoubleDown = remember(component) { { component.onDoubleDown() } }

    val gridCardState = rememberGridCardState(state)
    val gridSettings = rememberGridSettings(state)

    Box(modifier = modifier.fillMaxSize()) {
        GameGrid(
            gridCardState = gridCardState,
            settings = gridSettings,
            onCardClick = onCardClick,
            scorePositionInRoot = scorePosition,
        )

        GameHUD(
            state = rememberGameHUDState(state),
            useCompactUI = useCompactUI,
            onDoubleDown = onDoubleDown,
        )

        GameOverlays(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
        )
    }
}

@Composable
private fun BoxScope.GameHUD(
    state: GameHUDState,
    useCompactUI: Boolean,
    onDoubleDown: () -> Unit,
) {
    if (state.comboMultiplier > 1) {
        ComboBadge(
            state =
                ComboBadgeState(
                    combo = state.comboMultiplier,
                    isMegaBonus = state.isMegaBonus,
                    isHeatMode = state.isHeatMode,
                ),
            infiniteTransition = rememberInfiniteTransition(label = "comboBadge"),
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = PokerTheme.spacing.medium, end = PokerTheme.spacing.large),
            compact = useCompactUI,
        )
    }

    val mutatorTopPadding =
        (
            LayoutConstants.SPEECH_BUBBLE_TOP_PADDING_DP +
                LayoutConstants.MUTATOR_INDICATORS_TOP_OFFSET_DP
        ).dp

    MutatorIndicators(
        activeMutators = state.activeMutators,
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .padding(
                    top = mutatorTopPadding,
                    start = PokerTheme.spacing.medium,
                ),
        compact = useCompactUI,
    )

    if (state.isDoubleDownAvailable) {
        val audioService = LocalAppGraph.current.audioService
        DoubleDownButton(onDoubleDown, audioService)
    }

    DealerSpeechBubble(
        matchComment = state.matchComment,
        modifier =
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = LayoutConstants.SPEECH_BUBBLE_TOP_PADDING_DP.dp)
                .widthIn(max = LayoutConstants.DEALER_SPEECH_MAX_WIDTH_DP.dp),
    )
}

@Composable
private fun BoxScope.DoubleDownButton(
    onDoubleDown: () -> Unit,
    audioService: AudioService,
) {
    PokerButton(
        text = stringResource(Res.string.game_double_or_nothing),
        onClick = {
            audioService.playEffect(AudioService.SoundEffect.CLICK)
            onDoubleDown()
        },
        containerColor = PokerTheme.colors.tacticalRed,
        contentColor = PokerTheme.colors.goldenYellow,
        modifier =
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = LayoutConstants.DOUBLE_DOWN_BOTTOM_PADDING_DP.dp, end = LayoutConstants.DOUBLE_DOWN_END_PADDING_DP.dp),
    )
}

@Composable
private fun GameOverlays(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    if (state.isPeeking) {
        PeekCountdownOverlay(countdown = state.peekCountdown)
    }

    if (state.game.isGameOver) {
        GameGameOverOverlay(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
        )
    }

    if (state.showWalkthrough) {
        WalkthroughOverlay(
            step = state.walkthroughStep,
            onNext = { component.onWalkthroughAction(isComplete = false) },
            onDismiss = { component.onWalkthroughAction(isComplete = true) },
        )
    }
}
