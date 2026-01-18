package io.github.smithjustinn.ui.difficulty.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.GameModeOption
import io.github.smithjustinn.ui.difficulty.DifficultyState
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun DifficultySelectionSection(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 24.dp)
    ) {
        DifficultyCarousel(
            difficulties = state.difficulties,
            selectedDifficulty = state.selectedDifficulty,
            onDifficultySelected = onDifficultySelected,
            compact = compact
        )

        if (compact) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    GameModeSelection(
                        selectedMode = state.selectedMode,
                        onModeSelected = onModeSelected,
                        compact = true,
                        horizontalPadding = 0.dp
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    ActionButtons(
                        hasSavedGame = state.hasSavedGame,
                        onStartGame = onStartGame,
                        onResumeGame = onResumeGame,
                        compact = true,
                        horizontalPadding = 0.dp
                    )
                }
            }
        } else {
            GameModeSelection(
                selectedMode = state.selectedMode,
                onModeSelected = onModeSelected,
                compact = false
            )

            ActionButtons(
                hasSavedGame = state.hasSavedGame,
                onStartGame = onStartGame,
                onResumeGame = onResumeGame,
                compact = false
            )
        }
    }
}

@Composable
private fun DifficultyCarousel(
    difficulties: List<DifficultyLevel>,
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    compact: Boolean = false
) {
    Column(verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 12.dp)) {
        Text(
            text = stringResource(Res.string.how_many_pairs),
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 24.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(difficulties) { level ->
                DifficultyOptionCard(
                    level = level,
                    isSelected = selectedDifficulty == level,
                    onClick = { onDifficultySelected(level) },
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun GameModeSelection(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    compact: Boolean = false,
    horizontalPadding: androidx.compose.ui.unit.Dp = 24.dp
) {
    Column(
        modifier = Modifier.padding(horizontal = horizontalPadding),
        verticalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 12.dp)
    ) {
        Text(
            text = stringResource(Res.string.game_mode),
            style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(if (compact) 12.dp else 16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                .padding(if (compact) 2.dp else 4.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 2.dp else 4.dp)
        ) {
            GameModeOption(
                title = stringResource(Res.string.mode_standard),
                isSelected = selectedMode == GameMode.STANDARD,
                onClick = { onModeSelected(GameMode.STANDARD) },
                modifier = Modifier.weight(1f)
            )
            GameModeOption(
                title = stringResource(Res.string.mode_time_attack),
                isSelected = selectedMode == GameMode.TIME_ATTACK,
                onClick = { onModeSelected(GameMode.TIME_ATTACK) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun ActionButtons(
    hasSavedGame: Boolean,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    compact: Boolean = false,
    horizontalPadding: androidx.compose.ui.unit.Dp = 24.dp
) {
    val buttonHeight = if (compact) 44.dp else 64.dp
    
    Column(
        modifier = Modifier
            .padding(horizontal = horizontalPadding)
            .widthIn(max = 400.dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
    ) {
        if (hasSavedGame) {
            Button(
                onClick = onResumeGame,
                modifier = Modifier.fillMaxWidth().height(buttonHeight),
                shape = RoundedCornerShape(if (compact) 12.dp else 20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                contentPadding = if (compact) PaddingValues(horizontal = 8.dp) else ButtonDefaults.ContentPadding
            ) {
                Text(
                    text = stringResource(Res.string.resume_game),
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }

            OutlinedButton(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth().height(buttonHeight),
                shape = RoundedCornerShape(if (compact) 12.dp else 20.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                contentPadding = if (compact) PaddingValues(horizontal = 8.dp) else ButtonDefaults.ContentPadding
            ) {
                Text(
                    text = stringResource(Res.string.start),
                    style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
            }
        } else {
            Button(
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth().height(buttonHeight),
                shape = RoundedCornerShape(if (compact) 12.dp else 20.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
            ) {
                Text(
                    text = stringResource(Res.string.start),
                    style = if (compact) MaterialTheme.typography.labelLarge else MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun DifficultyOptionCard(
    level: DifficultyLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
    compact: Boolean = false
) {
    val scale by animateFloatAsState(if (isSelected) 1.05f else 1f)
    val containerColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    )
    val contentColor by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
    )

    Surface(
        onClick = onClick,
        modifier = Modifier
            .width(if (compact) 85.dp else 110.dp)
            .height(if (compact) 90.dp else 130.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(if (compact) 16.dp else 24.dp),
        color = containerColor,
        tonalElevation = if (isSelected) 8.dp else 0.dp,
        shadowElevation = if (isSelected) 12.dp else 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(if (compact) 4.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(level.nameRes),
                style = if (compact) MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp) else MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 1,
                color = contentColor
            )

            if (!compact) Spacer(Modifier.height(4.dp))

            Text(
                text = level.pairs.toString(),
                style = if (compact) MaterialTheme.typography.titleLarge else MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = contentColor
            )

            Text(
                text = stringResource(Res.string.pairs_label),
                style = MaterialTheme.typography.labelSmall.copy(fontSize = if (compact) 8.sp else 10.sp),
                fontWeight = FontWeight.Light,
                color = contentColor.copy(alpha = 0.8f)
            )
        }
    }
}
