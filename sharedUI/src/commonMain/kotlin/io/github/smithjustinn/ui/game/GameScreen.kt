package io.github.smithjustinn.ui.game

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.components.BouncingCardsOverlay
import io.github.smithjustinn.components.ConfettiEffect
import io.github.smithjustinn.components.ExplosionEffect
import io.github.smithjustinn.components.game.ExitGameDialog
import io.github.smithjustinn.components.game.GameGrid
import io.github.smithjustinn.components.game.GameTopBar
import io.github.smithjustinn.components.game.MatchCommentSnackbar
import io.github.smithjustinn.components.game.NewHighScoreSnackbar
import io.github.smithjustinn.components.game.ResultsCard
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.platform.CommonTransient
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.utils.BackPressScreen

data class GameScreen(val pairCount: Int) : Screen, BackPressScreen, JavaSerializable {

    @CommonTransient
    private var _model: GameScreenModel? = null

    override fun handleBack(): Boolean {
        val m = _model ?: return true
        if (m.state.value.game.isGameWon) return true
        m.handleIntent(GameIntent.SetExitDialogVisible(true))
        return false
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.gameScreenModel }
        _model = screenModel

        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(pairCount) {
            screenModel.handleIntent(GameIntent.StartGame(pairCount))
        }

        if (state.showExitDialog) {
            ExitGameDialog(
                onConfirm = {
                    screenModel.handleIntent(GameIntent.SetExitDialogVisible(false))
                    navigator.pop()
                },
                onDismiss = { screenModel.handleIntent(GameIntent.SetExitDialogVisible(false)) }
            )
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                GameTopBar(
                    score = state.game.score,
                    time = state.elapsedTimeSeconds,
                    bestScore = state.bestScore,
                    bestTime = state.bestTimeSeconds,
                    combo = state.game.comboMultiplier,
                    onBackClick = {
                        if (handleBack()) {
                            navigator.pop()
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                GameGrid(
                    cards = state.game.cards,
                    onCardClick = { cardId -> screenModel.handleIntent(GameIntent.FlipCard(cardId)) }
                )

                MatchCommentSnackbar(
                    matchComment = state.game.matchComment,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp, start = 16.dp, end = 16.dp)
                )

                if (state.showComboExplosion) {
                    ExplosionEffect(
                        modifier = Modifier.fillMaxSize(),
                        particleCount = 50
                    )
                }

                if (state.game.isGameWon) {
                    BouncingCardsOverlay(state.game.cards)
                    ConfettiEffect()

                    if (state.isNewHighScore) {
                        NewHighScoreSnackbar(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                        )
                    }

                    ResultsCard(
                        score = state.game.score,
                        moves = state.game.moves,
                        elapsedTimeSeconds = state.elapsedTimeSeconds,
                        scoreBreakdown = state.game.scoreBreakdown,
                        onPlayAgain = { screenModel.handleIntent(GameIntent.StartGame(pairCount)) },
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}
