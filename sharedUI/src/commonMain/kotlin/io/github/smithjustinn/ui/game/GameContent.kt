package io.github.smithjustinn.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AdaptiveDensity
import io.github.smithjustinn.ui.game.components.CardFountainOverlay
import io.github.smithjustinn.ui.game.components.ComboBadge
import io.github.smithjustinn.ui.game.components.ComboBadgeState
import io.github.smithjustinn.ui.game.components.ConfettiEffect
import io.github.smithjustinn.ui.game.components.DealerSpeechBubble
import io.github.smithjustinn.ui.game.components.GameGrid
import io.github.smithjustinn.ui.game.components.GameTopBar
import io.github.smithjustinn.ui.game.components.GameTopBarState
import io.github.smithjustinn.ui.game.components.GridCardState
import io.github.smithjustinn.ui.game.components.GridSettings
import io.github.smithjustinn.ui.game.components.NewHighScoreSnackbar
import io.github.smithjustinn.ui.game.components.ParticleEmbers
import io.github.smithjustinn.ui.game.components.PeekCountdownOverlay
import io.github.smithjustinn.ui.game.components.ResultsCard
import io.github.smithjustinn.ui.game.components.SteamEffect
import io.github.smithjustinn.ui.game.components.WalkthroughOverlay
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(
    component: GameComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    val scope = rememberCoroutineScope()
    val shakeOffset = remember { Animatable(0f) }
    var showSteam by remember { mutableStateOf(false) }
    var lastHeatMode by remember { mutableStateOf(state.isHeatMode) }

    GameEventHandler(
        component = component,
        onTheNuts = {
            // Re-use steam/shake effect for The Nuts
            showSteam = true
            scope.launch {
                shakeOffset.animateTo(20f, spring(stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(-20f, spring(stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(10f, spring(stiffness = Spring.StiffnessHigh))
                shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
            }
            scope.launch {
                delay(1200)
                showSteam = false
            }
        },
    )

    AdaptiveDensity {
        LaunchedEffect(state.isHeatMode) {
            if (lastHeatMode && !state.isHeatMode) {
                // LOST HEAT MODE -> SHAKE + STEAM
                showSteam = true
                launch {
                    // Quick double shake
                    shakeOffset.animateTo(20f, spring(stiffness = Spring.StiffnessHigh))
                    shakeOffset.animateTo(-20f, spring(stiffness = Spring.StiffnessHigh))
                    shakeOffset.animateTo(10f, spring(stiffness = Spring.StiffnessHigh))
                    shakeOffset.animateTo(0f, spring(stiffness = Spring.StiffnessMedium))
                }
                launch {
                    delay(1200) // Duration of steam effect
                    showSteam = false
                }
            }
            lastHeatMode = state.isHeatMode
        }

        BoxWithConstraints(
            modifier =
                modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = shakeOffset.value
                    },
        ) {
            val isLandscape = maxWidth > maxHeight
            val isCompactHeight = maxHeight < 500.dp
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
}

@Composable
private fun GameEventHandler(
    component: GameComponent,
    onTheNuts: () -> Unit,
) {
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val hapticsService = graph.hapticsService

    LaunchedEffect(Unit) {
        audioService.startMusic()
        component.events.collect { event ->
            when (event) {
                GameUiEvent.PlayFlip -> {
                    audioService.playEffect(AudioService.SoundEffect.FLIP)
                }

                GameUiEvent.PlayMatch -> {
                    audioService.playEffect(AudioService.SoundEffect.MATCH)
                }

                GameUiEvent.PlayMismatch -> {
                    audioService.playEffect(AudioService.SoundEffect.MISMATCH)
                }

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

                GameUiEvent.PlayHighScore -> {
                    audioService.playEffect(AudioService.SoundEffect.HIGH_SCORE)
                }

                GameUiEvent.PlayDeal -> {
                    audioService.playEffect(AudioService.SoundEffect.DEAL)
                }

                GameUiEvent.VibrateMatch -> {
                    hapticsService.vibrateMatch()
                }

                GameUiEvent.VibrateMismatch -> {
                    hapticsService.vibrateMismatch()
                }

                GameUiEvent.VibrateTick -> {
                    hapticsService.vibrateTick()
                }

                GameUiEvent.VibrateWarning -> {
                    hapticsService.vibrateWarning()
                }

                GameUiEvent.VibrateHeat -> {
                    hapticsService.vibrateHeat()
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            audioService.stopMusic()
        }
    }
}

@Composable
private fun GameGameOverOverlay(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
    ) {
        if (state.game.isGameWon) {
            GameWonOverlay(state)
        }

        GameResultsOverlay(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
        )
    }
}

@Composable
private fun BoxScope.GameWonOverlay(state: GameUIState) {
    CardFountainOverlay(
        cards = state.game.cards,
        settings = state.cardSettings,
    )
    ConfettiEffect()

    if (state.isNewHighScore) {
        NewHighScoreSnackbar(
            modifier =
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                    .widthIn(max = 500.dp),
        )
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
        score = state.game.score,
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
            val link =
                "memorymatch://game?mode=${state.game.mode}" +
                    "&pairs=${state.game.pairCount}&seed=$seed"
            scope.launch {
                clipboardManager.setText(AnnotatedString(link))
            }
            hapticsService.vibrateMatch()
        },
        onScoreTick = { hapticsService.vibrateTick() },
        modifier =
            Modifier
                .align(Alignment.Center)
                .widthIn(max = 550.dp)
                .padding(
                    vertical = if (useCompactUI) PokerTheme.spacing.small else PokerTheme.spacing.large,
                ),
        mode = state.game.mode,
    )
}

@Composable
private fun GameBackground(isHeatMode: Boolean) {
    val colors = PokerTheme.colors
    val backgroundTopColor by animateColorAsState(
        targetValue =
            if (isHeatMode) {
                colors.heatBackgroundTop
            } else {
                colors.feltGreen
            },
        animationSpec = tween(durationMillis = 800),
    )
    val backgroundBottomColor by animateColorAsState(
        targetValue =
            if (isHeatMode) {
                colors.heatBackgroundBottom
            } else {
                colors.feltGreenDark
            },
        animationSpec = tween(durationMillis = 800),
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
private fun GameMainScreen(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
) {
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    // Using fully qualified name to avoid import conflicts, standard Compose Offset
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
                            state.game.mode == io.github.smithjustinn.domain.models.GameMode.TIME_ATTACK &&
                                state.elapsedTimeSeconds <= GameTopBarState.LOW_TIME_THRESHOLD_SEC,
                        isCriticalTime =
                            state.game.mode == io.github.smithjustinn.domain.models.GameMode.TIME_ATTACK &&
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
            infiniteTransition = rememberInfiniteTransition(),
            modifier =
                Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = PokerTheme.spacing.medium, end = PokerTheme.spacing.large),
            compact = useCompactUI,
        )
    }

    // Double or Nothing Button - Only in Heat Mode and not already active
    if (state.isHeatMode && !state.game.isDoubleDownActive && !state.game.isGameOver) {
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
                    .padding(bottom = 100.dp, end = 16.dp),
            elevation =
                ButtonDefaults.buttonElevation(
                    defaultElevation = 6.dp,
                    pressedElevation = 2.dp,
                ),
        ) {
            Text(
                text = "DOUBLE OR NOTHING",
                style = PokerTheme.typography.labelMedium,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
            )
        }
    }

    DealerSpeechBubble(
        matchComment = state.game.matchComment,
        modifier =
            Modifier
                .align(Alignment.TopCenter)
                .padding(top = 80.dp) // Below TopBar approximately
                .widthIn(max = 600.dp),
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
