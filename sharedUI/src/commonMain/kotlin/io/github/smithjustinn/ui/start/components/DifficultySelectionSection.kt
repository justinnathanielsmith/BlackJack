package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.daily_challenge
import io.github.smithjustinn.resources.daily_challenge_completed
import io.github.smithjustinn.resources.game_mode
import io.github.smithjustinn.resources.mode_standard
import io.github.smithjustinn.resources.mode_time_attack
import io.github.smithjustinn.resources.resume_game
import io.github.smithjustinn.resources.select_difficulty
import io.github.smithjustinn.resources.start
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.PillSegmentedControl
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.components.PokerChip
import io.github.smithjustinn.ui.start.DifficultyState
import org.jetbrains.compose.resources.stringResource

/**
 * DifficultySelectionSection
 */
@Composable
fun DifficultySelectionSection(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(bottom = PokerTheme.spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
        ) {
            DifficultySelector(state, onDifficultySelected)
            ModeSelector(state, onModeSelected)
        }

        Spacer(modifier = Modifier.height(PokerTheme.spacing.huge))

        PrimaryActionButtons(
            state = state,
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
        )
    }
}

@Composable
private fun DifficultySelector(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
) {
    AppCard(title = stringResource(Res.string.select_difficulty)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            state.difficulties.forEach { level ->
                val chipColor =
                    when (level.pairs) {
                        PAIRS_EASY -> PokerTheme.colors.softBlue
                        PAIRS_MEDIUM -> PokerTheme.colors.tacticalRed
                        PAIRS_HARD -> PokerTheme.colors.bonusGreen
                        else -> Color.Black
                    }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.extraSmall),
                ) {
                    PokerChip(
                        text = level.pairs.toString(),
                        contentColor = chipColor,
                        isSelected = state.selectedDifficulty == level,
                        onClick = { onDifficultySelected(level) },
                    )

                    Text(
                        text = stringResource(level.nameRes),
                        style = PokerTheme.typography.labelSmall,
                        color =
                            if (state.selectedDifficulty == level) {
                                PokerTheme.colors.goldenYellow
                            } else {
                                PokerTheme.colors.goldenYellow.copy(alpha = 0.6f)
                            },
                        fontWeight =
                            if (state.selectedDifficulty == level) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Normal
                            },
                        textAlign = TextAlign.Center,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelector(
    state: DifficultyState,
    onModeSelected: (GameMode) -> Unit,
) {
    AppCard(title = stringResource(Res.string.game_mode)) {
        PillSegmentedControl(
            items = listOf(GameMode.STANDARD, GameMode.TIME_ATTACK),
            selectedItem = state.selectedMode,
            onItemSelected = onModeSelected,
            labelProvider = { mode ->
                when (mode) {
                    GameMode.STANDARD -> stringResource(Res.string.mode_standard)
                    GameMode.TIME_ATTACK -> stringResource(Res.string.mode_time_attack)
                    GameMode.DAILY_CHALLENGE -> stringResource(Res.string.daily_challenge)
                }
            },
        )
    }
}


@Composable
private fun PrimaryActionButtons(
    state: DifficultyState,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
    ) {
        val hasSavedGame = state.hasSavedGame

        if (hasSavedGame) {
            PokerButton(
                text = stringResource(Res.string.resume_game),
                onClick = onResumeGame,
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = AppIcons.ArrowBack,
                isPrimary = true,
                isPulsing = true,
            )

            PokerButton(
                text = stringResource(Res.string.start),
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = false,
                isPulsing = false,
            )
        } else {
            PokerButton(
                text = stringResource(Res.string.start),
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = true,
                isPulsing = true,
            )
        }
    }
}


private const val PAIRS_EASY = 6
private const val PAIRS_MEDIUM = 8
private const val PAIRS_HARD = 10
