package io.github.smithjustinn.ui.game

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
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
import io.github.smithjustinn.theme.HeatBackgroundBottom
import io.github.smithjustinn.theme.HeatBackgroundTop
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.components.AdaptiveDensity
import io.github.smithjustinn.ui.game.components.BouncingCardsOverlay
import io.github.smithjustinn.ui.game.components.ComboBadge
import io.github.smithjustinn.ui.game.components.ConfettiEffect
import io.github.smithjustinn.ui.game.components.GameGrid
import io.github.smithjustinn.ui.game.components.GameTopBar
import io.github.smithjustinn.ui.game.components.MatchCommentSnackbar
import io.github.smithjustinn.ui.game.components.NewHighScoreSnackbar
import io.github.smithjustinn.ui.game.components.PeekCountdownOverlay
import io.github.smithjustinn.ui.game.components.ResultsCard
import io.github.smithjustinn.ui.game.components.WalkthroughOverlay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GameContent(component: GameComponent, modifier: Modifier = Modifier) {
    val graph = LocalAppGraph.current
    val state by component.state.collectAsState()
    val audioService = graph.audioService
    val hapticsService = graph.hapticsService
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        audioService.startMusic()
        component.events.collect { event ->
            when (event) {
                GameUiEvent.PlayFlip -> {
                    audioService.playFlip()
                }

                GameUiEvent.PlayMatch -> {
                    audioService.playMatch()
                }

                GameUiEvent.PlayMismatch -> {
                    audioService.playMismatch()
                }

                GameUiEvent.PlayWin -> {
                    audioService.stopMusic()
                    audioService.playWin()
                }

                GameUiEvent.PlayLose -> {
                    audioService.stopMusic()
                    audioService.playLose()
                }

                GameUiEvent.PlayHighScore -> {
                    audioService.playHighScore()
                }

                GameUiEvent.PlayDeal -> {
                    audioService.playDeal()
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

    AdaptiveDensity {
        BoxWithConstraints(modifier = modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            val isCompactHeight = maxHeight < 500.dp
            val useCompactUI = isLandscape && isCompactHeight

            // Heat mode responsive background
            val backgroundTopColor by animateColorAsState(
                targetValue = if (state.isHeatMode) HeatBackgroundTop else StartBackgroundTop,
                animationSpec = tween(durationMillis = 800),
            )
            val backgroundBottomColor by animateColorAsState(
                targetValue = if (state.isHeatMode) HeatBackgroundBottom else StartBackgroundBottom,
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
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        GameTopBar(
                            time = state.elapsedTimeSeconds,
                            onBackClick = {
                                audioService.playClick()
                                component.onBack()
                            },
                            onRestartClick = {
                                audioService.playClick()
                                component.onRestart()
                                audioService.startMusic()
                            },
                            mode = state.game.mode,
                            maxTime = state.maxTimeSeconds,
                            showTimeGain = state.showTimeGain,
                            timeGainAmount = state.timeGainAmount,
                            showTimeLoss = state.showTimeLoss,
                            timeLossAmount = state.timeLossAmount,
                            isMegaBonus = state.isMegaBonus,
                            compact = useCompactUI,
                            isAudioEnabled = state.isMusicEnabled || state.isSoundEnabled,
                            onMuteClick = {
                                audioService.playClick()
                                component.onToggleAudio()
                            },
                        )
                    },
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
                        GameGrid(
                            cards = state.game.cards,
                            onCardClick = { cardId -> component.onFlipCard(cardId) },
                            isPeeking = state.isPeeking,
                            lastMatchedIds = state.game.lastMatchedIds,
                            showComboExplosion = state.showComboExplosion,
                            settings = state.cardSettings,
                        )

                        if (state.game.comboMultiplier > 1) {
                            ComboBadge(
                                combo = state.game.comboMultiplier,
                                isMegaBonus = state.isMegaBonus,
                                isHeatMode = state.isHeatMode,
                                infiniteTransition = rememberInfiniteTransition(),
                                modifier =
                                Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(top = 16.dp, end = 24.dp),
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
                                    bottom = if (useCompactUI) 0.dp else 32.dp,
                                    top = if (useCompactUI) 8.dp else 0.dp,
                                    start = 16.dp,
                                    end = 16.dp,
                                ).widthIn(max = 600.dp),
                        )

                        if (state.isPeeking) {
                            PeekCountdownOverlay(countdown = state.peekCountdown)
                        }

                        if (state.game.isGameOver) {
                            Box(
                                modifier =
                                Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f)),
                            )

                            if (state.game.isGameWon) {
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

                            ResultsCard(
                                isWon = state.game.isGameWon,
                                score = state.game.score,
                                moves = state.game.moves,
                                elapsedTimeSeconds = state.elapsedTimeSeconds,
                                scoreBreakdown = state.game.scoreBreakdown,
                                onPlayAgain = {
                                    audioService.playClick()
                                    component.onRestart()
                                    audioService.startMusic()
                                },
                                onShareReplay = {
                                    audioService.playClick()
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
                                        vertical = if (useCompactUI) 8.dp else 24.dp,
                                    ),
                                mode = state.game.mode,
                            )
                        }

                        if (state.showWalkthrough) {
                            WalkthroughOverlay(
                                step = state.walkthroughStep,
                                onNext = { component.onNextWalkthroughStep() },
                                onDismiss = { component.onCompleteWalkthrough() },
                            )
                        }
                    }
                }
            }
        }
    }
}
