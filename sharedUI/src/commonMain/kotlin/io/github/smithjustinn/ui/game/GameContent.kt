package io.github.smithjustinn.ui.game

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.game_double_or_nothing
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AdaptiveDensity
import io.github.smithjustinn.ui.game.components.ComboBadge
import io.github.smithjustinn.ui.game.components.ComboBadgeState
import io.github.smithjustinn.ui.game.components.DealerSpeechBubble
import io.github.smithjustinn.ui.game.components.GameGrid
import io.github.smithjustinn.ui.game.components.GameTopBar
import io.github.smithjustinn.ui.game.components.GameTopBarState
import io.github.smithjustinn.ui.game.components.GridCardState
import io.github.smithjustinn.ui.game.components.GridSettings
import io.github.smithjustinn.ui.game.components.ParticleEmbers
import io.github.smithjustinn.ui.game.components.PeekCountdownOverlay
import io.github.smithjustinn.ui.game.components.SteamEffect
import io.github.smithjustinn.ui.game.components.WalkthroughOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

private const val SHAKE_RESET_OFFSET = 0f
private const val STEAM_DURATION_MS = 1200
private const val COMPACT_HEIGHT_THRESHOLD_DP = 500
private const val DOUBLE_DOWN_BOTTOM_PADDING_DP = 100
private const val SPEECH_BUBBLE_TOP_PADDING_DP = 80

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(
    component: GameComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(SHAKE_RESET_OFFSET) }
    var showSteam by remember { mutableStateOf(false) }

    GameEventHandler(
        component = component,
        onTheNuts = {
            showSteam = true
            scope.launch {
                runShakeAnimation(shakeOffset)
            }
            scope.launch {
                delay(STEAM_DURATION_MS.toLong())
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
                    delay(STEAM_DURATION_MS.toLong())
                    showSteam = false
                }
            },
        )

        GameMainScreenWrapper(
            state = state,
            component = component,
            shakeOffset = shakeOffset.value,
            showSteam = showSteam,
            modifier = modifier,
        )
    }
}

@Composable
private fun GameMainScreenWrapper(
    state: GameUIState,
    component: GameComponent,
    shakeOffset: Float,
    showSteam: Boolean,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier =
            modifier
                .fillMaxSize()
                .graphicsLayer { translationX = shakeOffset },
    ) {
        val isLandscape = maxWidth > maxHeight
        val isCompactHeight = maxHeight < COMPACT_HEIGHT_THRESHOLD_DP.dp
        val useCompactUI = isLandscape && isCompactHeight

        Box(modifier = Modifier.fillMaxSize()) {
            GameBackground(isHeatMode = state.isHeatMode)
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

@Composable
private fun GameEventHandler(
    component: GameComponent,
    onTheNuts: () -> Unit,
) {
    val currentOnTheNuts by rememberUpdatedState(onTheNuts)
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val hapticsService = graph.hapticsService

    LaunchedEffect(Unit) {
        audioService.startMusic()
        component.events.collect { event ->
            handleGameEvent(event, audioService, hapticsService, currentOnTheNuts)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioService.stopMusic()
        }
    }
}

private fun handleGameEvent(
    event: GameUiEvent,
    audioService: AudioService,
    hapticsService: io.github.smithjustinn.services.HapticsService,
    onTheNuts: () -> Unit,
) {
    when (event) {
        GameUiEvent.PlayFlip -> audioService.playEffect(AudioService.SoundEffect.FLIP)
        GameUiEvent.PlayMatch -> audioService.playEffect(AudioService.SoundEffect.MATCH)
        GameUiEvent.PlayMismatch -> audioService.playEffect(AudioService.SoundEffect.MISMATCH)
        GameUiEvent.PlayTheNuts -> {
            audioService.playEffect(AudioService.SoundEffect.THE_NUTS)
            onTheNuts()
        }
        GameUiEvent.PlayWin -> {
            audioService.stopMusic()
            audioService.playEffect(AudioService.SoundEffect.WIN)
        }
        GameUiEvent.PlayLose -> {
            audioService.stopMusic()
            audioService.playEffect(AudioService.SoundEffect.LOSE)
        }
        GameUiEvent.PlayHighScore -> audioService.playEffect(AudioService.SoundEffect.HIGH_SCORE)
        GameUiEvent.PlayDeal -> audioService.playEffect(AudioService.SoundEffect.DEAL)
        GameUiEvent.VibrateMatch -> hapticsService.vibrateMatch()
        GameUiEvent.VibrateMismatch -> hapticsService.vibrateMismatch()
        GameUiEvent.VibrateTick -> hapticsService.vibrateTick()
        GameUiEvent.VibrateWarning -> hapticsService.vibrateWarning()
        GameUiEvent.VibrateHeat -> hapticsService.vibrateHeat()
    }
}

@Composable
private fun GameMainScreen(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    var scorePosition by remember { mutableStateOf(androidx.compose.ui.geometry.Offset.Zero) }

    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            GameTopBar(
                state =
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
                        score = state.game.score,
                        isHeatMode = state.isHeatMode,
                    ),
                onBackClick = {
                    audioService.playEffect(AudioService.SoundEffect.CLICK)
                    component.onBack()
                },
                onRestartClick = {
                    audioService.playEffect(AudioService.SoundEffect.CLICK)
                    component.onRestart()
                    audioService.startMusic()
                },
                onMuteClick = {
                    audioService.playEffect(AudioService.SoundEffect.CLICK)
                    component.onToggleAudio()
                },
                onScorePositioned = { scorePosition = it },
            )
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
private fun GameMainContent(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
    scorePosition: androidx.compose.ui.geometry.Offset,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        GameGrid(
            gridCardState =
                GridCardState(
                    cards = state.game.cards,
                    lastMatchedIds = state.game.lastMatchedIds,
                    isPeeking = state.isPeeking,
                ),
            settings =
                GridSettings(
                    displaySettings = state.cardSettings,
                    showComboExplosion = state.showComboExplosion,
                ),
            onCardClick = { cardId -> component.onFlipCard(cardId) },
            scorePositionInRoot = scorePosition,
        )

        GameHUD(
            state = state,
            useCompactUI = useCompactUI,
            onDoubleDown = { component.onDoubleDown() },
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
    state: GameUIState,
    useCompactUI: Boolean,
    onDoubleDown: () -> Unit,
) {
    if (state.game.comboMultiplier > 1) {
        ComboBadge(
            state =
                ComboBadgeState(
                    combo = state.game.comboMultiplier,
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

    if (state.isDoubleDownAvailable) {
        DoubleDownButton(onDoubleDown)
    }

    DealerSpeechBubble(
        matchComment = state.game.matchComment,
        modifier =
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = SPEECH_BUBBLE_TOP_PADDING_DP.dp)
                .widthIn(max = 600.dp),
    )
}

@Composable
private fun BoxScope.DoubleDownButton(onDoubleDown: () -> Unit) {
    Button(
        onClick = onDoubleDown,
        colors =
            ButtonDefaults.buttonColors(
                containerColor = PokerTheme.colors.tacticalRed,
                contentColor = PokerTheme.colors.goldenYellow,
            ),
        modifier =
            Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = DOUBLE_DOWN_BOTTOM_PADDING_DP.dp, end = 16.dp),
        elevation =
            ButtonDefaults.buttonElevation(
                defaultElevation = 6.dp,
                pressedElevation = 2.dp,
            ),
    ) {
        Text(
            text = stringResource(Res.string.game_double_or_nothing),
            style = PokerTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
        )
    }
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
