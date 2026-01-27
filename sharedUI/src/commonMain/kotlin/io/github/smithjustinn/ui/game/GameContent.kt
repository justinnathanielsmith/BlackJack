package io.github.smithjustinn.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AdaptiveDensity
import io.github.smithjustinn.ui.game.components.BouncingCardsOverlay
import io.github.smithjustinn.ui.game.components.ComboBadge
import io.github.smithjustinn.ui.game.components.ComboBadgeState
import io.github.smithjustinn.ui.game.components.ConfettiEffect
import io.github.smithjustinn.ui.game.components.GameGrid
import io.github.smithjustinn.ui.game.components.GameTopBar
import io.github.smithjustinn.ui.game.components.GameTopBarState
import io.github.smithjustinn.ui.game.components.GridCardState
import io.github.smithjustinn.ui.game.components.GridSettings
import io.github.smithjustinn.ui.game.components.MatchCommentSnackbar
import io.github.smithjustinn.ui.game.components.NewHighScoreSnackbar
import io.github.smithjustinn.ui.game.components.PeekCountdownOverlay
import io.github.smithjustinn.ui.game.components.ResultsCard
import io.github.smithjustinn.ui.game.components.WalkthroughOverlay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(
    component: GameComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()

    GameEventHandler(component)

    AdaptiveDensity {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
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
            }
        }
    }
}

@Composable
private fun GameEventHandler(component: GameComponent) {
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
    BouncingCardsOverlay(
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
            )
        },
    ) { paddingValues ->
        GameMainContent(
            state = state,
            component = component,
            useCompactUI = useCompactUI,
            modifier = Modifier.padding(top = paddingValues.calculateTopPadding()),
        )
    }
}

@Composable
private fun GameMainContent(
    state: GameUIState,
    component: GameComponent,
    useCompactUI: Boolean,
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
        )

        GameHUD(state = state, useCompactUI = useCompactUI)

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

    MatchCommentSnackbar(
        matchComment = state.game.matchComment,
        modifier =
            Modifier
                .align(if (useCompactUI) Alignment.TopCenter else Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(
                    bottom = if (useCompactUI) PokerTheme.spacing.none else PokerTheme.spacing.extraLarge,
                    top = if (useCompactUI) PokerTheme.spacing.small else PokerTheme.spacing.none,
                    start = PokerTheme.spacing.medium,
                    end = PokerTheme.spacing.medium,
                ).widthIn(max = 600.dp),
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
