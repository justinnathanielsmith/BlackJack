package io.github.smithjustinn.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import dev.zacsweers.metro.Inject
import io.github.smithjustinn.components.AppIcons
import io.github.smithjustinn.components.BouncingCardsOverlay
import io.github.smithjustinn.components.ConfettiEffect
import io.github.smithjustinn.components.ExplosionEffect
import io.github.smithjustinn.components.PlayingCard
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.MemoryGameLogic
import io.github.smithjustinn.domain.models.CardState
import io.github.smithjustinn.domain.models.GameDomainEvent
import io.github.smithjustinn.domain.models.GameStats
import io.github.smithjustinn.domain.models.LeaderboardEntry
import io.github.smithjustinn.domain.models.MatchComment
import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.domain.models.ScoreBreakdown
import io.github.smithjustinn.domain.repositories.GameStateRepository
import io.github.smithjustinn.domain.repositories.GameStatsRepository
import io.github.smithjustinn.domain.repositories.LeaderboardRepository
import io.github.smithjustinn.platform.CommonTransient
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.services.HapticsService
import io.github.smithjustinn.utils.formatTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import memory_match.sharedui.generated.resources.back_content_description
import memory_match.sharedui.generated.resources.best_score_label
import memory_match.sharedui.generated.resources.best_time_label
import memory_match.sharedui.generated.resources.cancel
import memory_match.sharedui.generated.resources.combo_format
import memory_match.sharedui.generated.resources.final_score
import memory_match.sharedui.generated.resources.game_complete
import memory_match.sharedui.generated.resources.moves_label
import memory_match.sharedui.generated.resources.new_high_score
import memory_match.sharedui.generated.resources.play_again
import memory_match.sharedui.generated.resources.quit_confirm
import memory_match.sharedui.generated.resources.quit_game_message
import memory_match.sharedui.generated.resources.quit_game_title
import memory_match.sharedui.generated.resources.score_breakdown_title
import memory_match.sharedui.generated.resources.score_label
import memory_match.sharedui.generated.resources.score_match_points
import memory_match.sharedui.generated.resources.score_move_bonus
import memory_match.sharedui.generated.resources.score_time_bonus
import memory_match.sharedui.generated.resources.time_label
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock

/**
 * Represents the overall state of the game screen, including the UI-only state.
 */
data class GameUIState(
    val game: MemoryGameState = MemoryGameState(),
    val showExitDialog: Boolean = false,
    val elapsedTimeSeconds: Long = 0,
    val bestScore: Int = 0,
    val bestTimeSeconds: Long = 0,
    val showComboExplosion: Boolean = false,
    val isNewHighScore: Boolean = false
)

/**
 * Sealed class representing user intents for the game screen.
 */
sealed class GameIntent {
    data class StartGame(val pairCount: Int) : GameIntent()
    data class FlipCard(val cardId: Int) : GameIntent()
    data class SetExitDialogVisible(val visible: Boolean) : GameIntent()
    data object SaveGame : GameIntent()
}

@Inject
class GameScreenModel(
    private val hapticsService: HapticsService,
    private val gameStatsRepository: GameStatsRepository,
    private val leaderboardRepository: LeaderboardRepository,
    private val gameStateRepository: GameStateRepository
) : ScreenModel {
    private val _state = MutableStateFlow(GameUIState())
    val state: StateFlow<GameUIState> = _state.asStateFlow()
    
    private var timerJob: Job? = null
    private var commentJob: Job? = null
    private var statsJob: Job? = null
    private var explosionJob: Job? = null

    fun handleIntent(intent: GameIntent) {
        when (intent) {
            is GameIntent.StartGame -> startGame(intent.pairCount)
            is GameIntent.FlipCard -> flipCard(intent.cardId)
            is GameIntent.SetExitDialogVisible -> _state.update { it.copy(showExitDialog = intent.visible) }
            is GameIntent.SaveGame -> saveGame()
        }
    }

    private fun startGame(pairCount: Int) {
        screenModelScope.launch {
            val savedGame = gameStateRepository.getSavedGameState()
            if (savedGame != null && savedGame.first.pairCount == pairCount && !savedGame.first.isGameWon) {
                _state.update {
                    it.copy(
                        game = savedGame.first,
                        elapsedTimeSeconds = savedGame.second,
                        showComboExplosion = false,
                        isNewHighScore = false
                    )
                }
            } else {
                val initialGameState = MemoryGameLogic.createInitialState(pairCount)
                _state.update { 
                    it.copy(
                        game = initialGameState,
                        elapsedTimeSeconds = 0,
                        showComboExplosion = false,
                        isNewHighScore = false
                    ) 
                }
            }
            
            observeStats(pairCount)
            startTimer()
        }
    }

    private fun observeStats(pairCount: Int) {
        statsJob?.cancel()
        statsJob = screenModelScope.launch {
            gameStatsRepository.getStatsForDifficulty(pairCount).collect { stats ->
                _state.update { it.copy(
                    bestScore = stats?.bestScore ?: 0,
                    bestTimeSeconds = stats?.bestTimeSeconds ?: 0
                ) }
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = screenModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(1000)
                _state.update { it.copy(elapsedTimeSeconds = it.elapsedTimeSeconds + 1) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun flipCard(cardId: Int) {
        val (newState, event) = MemoryGameLogic.flipCard(_state.value.game, cardId)
        
        _state.update { it.copy(game = newState) }
        saveGame()

        when (event) {
            GameDomainEvent.MatchSuccess -> handleMatchSuccess(newState)
            GameDomainEvent.MatchFailure -> handleMatchFailure()
            GameDomainEvent.GameWon -> handleGameWon(newState)
            null -> {}
        }
    }

    private fun handleMatchSuccess(newState: MemoryGameState) {
        hapticsService.vibrateMatch()
        clearCommentAfterDelay()
        if (newState.comboMultiplier > 2) {
            triggerComboExplosion()
        }
    }

    private fun handleMatchFailure() {
        hapticsService.vibrateMismatch()
        screenModelScope.launch {
            delay(1000)
            _state.update { it.copy(game = MemoryGameLogic.resetErrorCards(it.game)) }
        }
    }

    private fun handleGameWon(newState: MemoryGameState) {
        hapticsService.vibrateMatch()
        stopTimer()
        
        val gameWithBonuses = MemoryGameLogic.applyFinalBonuses(newState, _state.value.elapsedTimeSeconds)
        val isNewHigh = gameWithBonuses.score > _state.value.bestScore
        
        _state.update { it.copy(
            game = gameWithBonuses,
            isNewHighScore = isNewHigh
        ) }
        
        saveStats(gameWithBonuses.pairCount, gameWithBonuses.score, _state.value.elapsedTimeSeconds, gameWithBonuses.moves)
        clearCommentAfterDelay()
        
        screenModelScope.launch {
            gameStateRepository.clearSavedGameState()
        }
    }

    private fun triggerComboExplosion() {
        explosionJob?.cancel()
        explosionJob = screenModelScope.launch {
            _state.update { it.copy(showComboExplosion = true) }
            delay(1000)
            _state.update { it.copy(showComboExplosion = false) }
        }
    }

    private fun clearCommentAfterDelay() {
        commentJob?.cancel()
        commentJob = screenModelScope.launch {
            delay(2500)
            _state.update { it.copy(game = it.game.copy(matchComment = null)) }
        }
    }

    private fun saveStats(pairCount: Int, score: Int, time: Long, moves: Int) {
        screenModelScope.launch(Dispatchers.IO) {
            val currentBestScore = _state.value.bestScore
            val currentBestTime = _state.value.bestTimeSeconds
            
            val newBestScore = if (score > currentBestScore) score else currentBestScore
            val newBestTime = if (currentBestTime == 0L || time < currentBestTime) time else currentBestTime
            
            gameStatsRepository.updateStats(GameStats(pairCount, newBestScore, newBestTime))
            
            leaderboardRepository.addEntry(
                LeaderboardEntry(
                    pairCount = pairCount,
                    score = score,
                    timeSeconds = time,
                    moves = moves,
                    timestamp = Clock.System.now()
                )
            )
        }
    }
    
    private fun saveGame() {
        val currentState = _state.value
        if (!currentState.game.isGameWon) {
            screenModelScope.launch {
                gameStateRepository.saveGameState(currentState.game, currentState.elapsedTimeSeconds)
            }
        }
    }

    override fun onDispose() {
        stopTimer()
        commentJob?.cancel()
        statsJob?.cancel()
        explosionJob?.cancel()
        saveGame()
    }
}

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

@Composable
private fun NewHighScoreSnackbar(
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(AppIcons.Info, contentDescription = null)
            Text(
                text = stringResource(Res.string.new_high_score),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun MatchCommentSnackbar(
    matchComment: MatchComment?,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = matchComment != null,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
        modifier = modifier
    ) {
        if (matchComment != null) {
            val commentText = stringResource(matchComment.res, *matchComment.args.toTypedArray())
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.inverseSurface,
                    contentColor = MaterialTheme.colorScheme.inverseOnSurface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = commentText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GameTopBar(
    score: Int,
    time: Long,
    bestScore: Int,
    bestTime: Long,
    combo: Int,
    onBackClick: () -> Unit
) {
    TopAppBar(
        title = {
            Column {
                Text(stringResource(Res.string.app_name), style = MaterialTheme.typography.titleMedium)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(Res.string.score_label, score),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (bestScore > 0) {
                            Text(
                                text = stringResource(Res.string.best_score_label, bestScore),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }

                    Column {
                        Text(
                            text = stringResource(Res.string.time_label, formatTime(time)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (bestTime > 0) {
                            Text(
                                text = stringResource(Res.string.best_time_label, formatTime(bestTime)),
                                style = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        },
        actions = {
            if (combo > 1) {
                Text(
                    text = stringResource(Res.string.combo_format, combo),
                    modifier = Modifier.padding(end = 16.dp),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(AppIcons.ArrowBack, contentDescription = stringResource(Res.string.back_content_description))
            }
        }
    )
}

@Composable
private fun GameGrid(
    cards: List<CardState>,
    onCardClick: (Int) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 80.dp),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        items(cards, key = { it.id }) { card ->
            PlayingCard(
                suit = card.suit,
                rank = card.rank,
                isFaceUp = card.isFaceUp,
                isMatched = card.isMatched,
                isError = card.isError,
                onClick = { onCardClick(card.id) }
            )
        }
    }
}

@Composable
private fun ResultsCard(
    score: Int,
    moves: Int,
    elapsedTimeSeconds: Long,
    scoreBreakdown: ScoreBreakdown,
    onPlayAgain: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(32.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f)
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.game_complete),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(Res.string.final_score, score),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(Res.string.time_label, formatTime(elapsedTimeSeconds)),
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = stringResource(Res.string.moves_label, moves),
                    style = MaterialTheme.typography.titleLarge
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(Res.string.score_breakdown_title),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = stringResource(Res.string.score_match_points, scoreBreakdown.matchPoints),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = stringResource(Res.string.score_time_bonus, scoreBreakdown.timeBonus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(Res.string.score_move_bonus, scoreBreakdown.moveBonus),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onPlayAgain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(Res.string.play_again), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ExitGameDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(Res.string.quit_game_title)) },
        text = { Text(stringResource(Res.string.quit_game_message)) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(stringResource(Res.string.quit_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        }
    )
}
