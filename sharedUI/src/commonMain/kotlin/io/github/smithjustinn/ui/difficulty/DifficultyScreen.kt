package io.github.smithjustinn.ui.difficulty

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.components.AppIcons
import io.github.smithjustinn.components.PlayingCard
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.Rank
import io.github.smithjustinn.domain.models.Suit
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.ui.game.GameScreen
import io.github.smithjustinn.ui.stats.StatsScreen
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import memory_match.sharedui.generated.resources.how_many_pairs
import memory_match.sharedui.generated.resources.pairs_format
import memory_match.sharedui.generated.resources.resume_game
import memory_match.sharedui.generated.resources.select_difficulty
import memory_match.sharedui.generated.resources.start
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
                        IconButton(onClick = { navigator.push(StatsScreen()) }) {
                            Icon(AppIcons.Info, contentDescription = "Stats")
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
                        screenModel.handleIntent(DifficultyIntent.SelectDifficulty(level))
                    },
                    onStartGame = {
                        screenModel.handleIntent(DifficultyIntent.StartGame(state.selectedDifficulty.pairs)) { pairs ->
                            navigator.push(GameScreen(pairs))
                        }
                    },
                    onResumeGame = {
                        screenModel.handleIntent(DifficultyIntent.ResumeGame) { pairs ->
                            navigator.push(GameScreen(pairs))
                        }
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
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = modifier.widthIn(max = 400.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Text(
            text = stringResource(Res.string.how_many_pairs),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
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

        AnimatedVisibility(
            visible = state.hasSavedGame,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Button(
                onClick = onResumeGame,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = MaterialTheme.shapes.medium,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text(
                    text = stringResource(Res.string.resume_game),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
