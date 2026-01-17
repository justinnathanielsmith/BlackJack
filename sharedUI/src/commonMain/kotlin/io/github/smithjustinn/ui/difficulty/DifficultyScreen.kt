package io.github.smithjustinn.ui.difficulty

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.ui.game.GameScreen
import io.github.smithjustinn.ui.game.components.PlayingCard
import io.github.smithjustinn.ui.settings.SettingsScreen
import io.github.smithjustinn.ui.stats.StatsScreen
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

class DifficultyScreen : Screen, JavaSerializable {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.difficultyScreenModel }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow

        LaunchedEffect(Unit) {
            screenModel.handleIntent(DifficultyIntent.CheckSavedGame)
        }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                TopAppBar(
                    title = {},
                    actions = {
                        IconButton(onClick = { 
                            graph.audioService.playClick()
                            navigator.push(SettingsScreen()) 
                        }) {
                            Icon(AppIcons.Settings, contentDescription = stringResource(Res.string.settings))
                        }
                        IconButton(onClick = { 
                            graph.audioService.playClick()
                            navigator.push(StatsScreen()) 
                        }) {
                            Icon(AppIcons.Info, contentDescription = stringResource(Res.string.stats))
                        }
                    }
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
            ) {
                DifficultyHeader()

                CardPreview()

                DifficultySelectionSection(
                    state = state,
                    onDifficultySelected = { level ->
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.SelectDifficulty(level))
                    },
                    onModeSelected = { mode ->
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.SelectMode(mode))
                    },
                    onStartGame = {
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.StartGame(state.selectedDifficulty.pairs, state.selectedMode)) { pairs, mode ->
                            navigator.push(GameScreen(pairs, forceNewGame = true, mode = mode))
                        }
                    },
                    onResumeGame = {
                        graph.audioService.playClick()
                        screenModel.handleIntent(DifficultyIntent.ResumeGame) { pairs, mode ->
                            navigator.push(GameScreen(pairs, forceNewGame = false, mode = mode))
                        }
                    },
                    onExpandChange = {
                        graph.audioService.playClick()
                    }
                )
            }
        }
    }
}

@Composable
private fun DifficultyHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        Text(
            text = stringResource(Res.string.select_difficulty),
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CardPreview(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "sway")
    val swayAngle by infiniteTransition.animateFloat(
        initialValue = -5f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "swayAngle"
    )

    Box(
        modifier = modifier
            .height(180.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlayingCard(
                suit = Suit.Hearts,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = true,
                modifier = Modifier
                    .offset(x = 15.dp)
                    .graphicsLayer {
                        rotationZ = -12f + swayAngle
                    }
            )
            PlayingCard(
                suit = Suit.Spades,
                rank = Rank.Ace,
                isFaceUp = true,
                isMatched = true,
                modifier = Modifier
                    .offset(x = (-15).dp)
                    .graphicsLayer {
                        rotationZ = 12f + swayAngle
                    }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DifficultySelectionSection(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    onExpandChange: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.widthIn(max = 400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.how_many_pairs),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { 
                expanded = !expanded 
                onExpandChange()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = stringResource(state.selectedDifficulty.nameRes),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(Res.string.select_difficulty)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .menuAnchor(type = ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled = true)
                    .fillMaxWidth()
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                state.difficulties.forEach { level ->
                    DropdownMenuItem(
                        text = {
                            Column {
                                Text(
                                    text = stringResource(level.nameRes),
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(
                                    text = stringResource(Res.string.pairs_format, level.pairs),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        onClick = {
                            onDifficultySelected(level)
                            expanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                    )
                }
            }
        }

        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = state.selectedMode == GameMode.STANDARD,
                onClick = { onModeSelected(GameMode.STANDARD) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
                Text("Standard")
            }
            SegmentedButton(
                selected = state.selectedMode == GameMode.TIME_ATTACK,
                onClick = { onModeSelected(GameMode.TIME_ATTACK) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
                Text("Time Attack")
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.hasSavedGame) {
                Button(
                    onClick = onResumeGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Text(
                        text = stringResource(Res.string.resume_game),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(Res.string.start),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else {
                Button(
                    onClick = onStartGame,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = stringResource(Res.string.start),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
