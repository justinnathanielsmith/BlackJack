package io.github.smithjustinn.ui.game

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.platform.CommonTransient
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.ui.game.components.*
import io.github.smithjustinn.utils.BackPressScreen

data class GameScreen(
    val pairCount: Int,
    val forceNewGame: Boolean = false,
    val mode: GameMode = GameMode.STANDARD
) : Screen, BackPressScreen, JavaSerializable {

    @CommonTransient
    private var _model: GameScreenModel? = null

    override fun handleBack(): Boolean {
        return true
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.gameScreenModel }
        _model = screenModel

        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val audioService = graph.audioService
        val hapticsService = graph.hapticsService

        LaunchedEffect(pairCount, forceNewGame, mode) {
            screenModel.handleIntent(GameIntent.StartGame(pairCount, forceNewGame, mode))
        }

        LaunchedEffect(Unit) {
            audioService.startMusic()
            screenModel.events.collect { event ->
                when (event) {
                    GameUiEvent.PlayFlip -> audioService.playFlip()
                    GameUiEvent.PlayMatch -> audioService.playMatch()
                    GameUiEvent.PlayMismatch -> audioService.playMismatch()
                    GameUiEvent.PlayWin -> {
                        audioService.stopMusic()
                        audioService.playWin()
                    }
                    GameUiEvent.PlayLose -> {
                        audioService.stopMusic()
                        audioService.playLose()
                    }
                    GameUiEvent.PlayHighScore -> audioService.playHighScore()
                    GameUiEvent.PlayDeal -> audioService.playDeal()
                    GameUiEvent.VibrateMatch -> hapticsService.vibrateMatch()
                    GameUiEvent.VibrateMismatch -> hapticsService.vibrateMismatch()
                    GameUiEvent.VibrateTick -> hapticsService.vibrateTick()
                    GameUiEvent.VibrateWarning -> hapticsService.vibrateWarning()
                }
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                audioService.stopMusic()
            }
        }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight
            val isCompactHeight = maxHeight < 500.dp
            val useCompactUI = isLandscape && isCompactHeight

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                                MaterialTheme.colorScheme.surface
                            )
                        )
                    )
            ) {
                Scaffold(
                    containerColor = Color.Transparent,
                    modifier = Modifier.fillMaxSize(),
                    contentWindowInsets = WindowInsets(0, 0, 0, 0),
                    topBar = {
                        GameTopBar(
                            score = state.game.score,
                            time = state.elapsedTimeSeconds,
                            bestScore = state.bestScore,
                            combo = state.game.comboMultiplier,
                            onBackClick = {
                                audioService.playClick()
                                navigator.pop()
                            },
                            onRestartClick = {
                                audioService.playClick()
                                screenModel.handleIntent(GameIntent.StartGame(pairCount, forceNewGame = true, mode = mode))
                                audioService.startMusic()
                            },
                            isPeeking = state.isPeeking,
                            mode = mode,
                            maxTime = state.maxTimeSeconds,
                            showTimeGain = state.showTimeGain,
                            timeGainAmount = totalGainAmount(state),
                            showTimeLoss = state.showTimeLoss,
                            timeLossAmount = state.timeLossAmount,
                            isMegaBonus = state.isMegaBonus,
                            compact = useCompactUI,
                            isGameOver = state.game.isGameOver,
                            isAudioEnabled = state.isMusicEnabled || state.isSoundEnabled,
                            onMuteClick = {
                                audioService.playClick()
                                screenModel.handleIntent(GameIntent.ToggleAudio)
                            }
                        )
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.fillMaxSize().padding(top = paddingValues.calculateTopPadding())) {
                        GameGrid(
                            cards = state.game.cards,
                            onCardClick = { cardId -> screenModel.handleIntent(GameIntent.FlipCard(cardId)) },
                            isPeeking = state.isPeeking,
                            lastMatchedIds = state.game.lastMatchedIds,
                            showComboExplosion = state.showComboExplosion,
                            cardBackTheme = state.cardBackTheme,
                            cardSymbolTheme = state.cardSymbolTheme,
                            areSuitsMultiColored = state.areSuitsMultiColored
                        )

                        MatchCommentSnackbar(
                            matchComment = state.game.matchComment,
                            modifier = Modifier
                                .align(if (useCompactUI) Alignment.TopCenter else Alignment.BottomCenter)
                                .navigationBarsPadding()
                                .padding(
                                    bottom = if (useCompactUI) 0.dp else 32.dp,
                                    top = if (useCompactUI) 8.dp else 0.dp,
                                    start = 16.dp, 
                                    end = 16.dp
                                )
                                .widthIn(max = 600.dp)
                        )

                        if (state.isPeeking) {
                            PeekCountdownOverlay(countdown = state.peekCountdown)
                        }

                        if (state.game.isGameOver) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.4f))
                            )

                            if (state.game.isGameWon) {
                                BouncingCardsOverlay(
                                    cards = state.game.cards,
                                    cardBackTheme = state.cardBackTheme,
                                    cardSymbolTheme = state.cardSymbolTheme,
                                    areSuitsMultiColored = state.areSuitsMultiColored
                                )
                                ConfettiEffect()

                                if (state.isNewHighScore) {
                                    NewHighScoreSnackbar(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                                            .widthIn(max = 500.dp)
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
                                    screenModel.handleIntent(GameIntent.StartGame(pairCount, forceNewGame = true, mode = mode))
                                    audioService.startMusic()
                                },
                                onScoreTick = { hapticsService.vibrateTick() },
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .widthIn(max = 550.dp)
                                    .padding(vertical = if (useCompactUI) 8.dp else 24.dp),
                                mode = mode
                            )
                        }

                        if (state.showWalkthrough) {
                            WalkthroughOverlay(
                                step = state.walkthroughStep,
                                onNext = { screenModel.handleIntent(GameIntent.NextWalkthroughStep) },
                                onDismiss = { screenModel.handleIntent(GameIntent.CompleteWalkthrough) }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun totalGainAmount(state: GameUIState): Int {
        return state.timeGainAmount
    }
}
