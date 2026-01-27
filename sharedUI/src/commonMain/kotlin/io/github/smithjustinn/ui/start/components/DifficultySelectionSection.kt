package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.daily_challenge
import io.github.smithjustinn.resources.daily_challenge_completed
import io.github.smithjustinn.resources.game_mode
import io.github.smithjustinn.resources.leaderboard
import io.github.smithjustinn.resources.mode_standard
import io.github.smithjustinn.resources.mode_time_attack
import io.github.smithjustinn.resources.resume_game
import io.github.smithjustinn.resources.select_difficulty
import io.github.smithjustinn.resources.settings
import io.github.smithjustinn.resources.start
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.PillSegmentedControl
import io.github.smithjustinn.ui.components.PokerButton
import io.github.smithjustinn.ui.components.PokerChip
import io.github.smithjustinn.ui.start.DifficultyState
import io.github.smithjustinn.ui.theme.PokerTheme
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
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            DifficultySelector(state, onDifficultySelected)
            ModeSelector(state, onModeSelected)
        }

        Spacer(modifier = Modifier.height(48.dp))
        // Action Buttons
        ActionButtons(
            state = state,
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
            onSettingsClick = onSettingsClick,
            onStatsClick = onStatsClick,
            onDailyChallengeClick = onDailyChallengeClick,
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
                        6 -> PokerTheme.ChipBlue
                        8 -> PokerTheme.ChipRed
                        10 -> PokerTheme.ChipGreen
                        else -> PokerTheme.ChipBlack
                    }

                PokerChip(
                    text = level.pairs.toString(),
                    contentColor = chipColor,
                    isSelected = state.selectedDifficulty == level,
                    onClick = { onDifficultySelected(level) },
                )
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
private fun ActionButtons(
    state: DifficultyState,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onDailyChallengeClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Primary Actions: Start / Resume
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PokerButton(
                text = stringResource(Res.string.start),
                onClick = onStartGame,
                modifier = Modifier.fillMaxWidth(),
            )

            if (state.hasSavedGame) {
                PokerButton(
                    text = stringResource(Res.string.resume_game),
                    onClick = onResumeGame,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = AppIcons.ArrowBack,
                )
            }
        }

        // Secondary Actions: Settings, Stats, Daily
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                PokerButton(
                    text = stringResource(Res.string.settings),
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f),
                    leadingIcon = AppIcons.Settings,
                )
                PokerButton(
                    text = stringResource(Res.string.leaderboard),
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f),
                    leadingIcon = AppIcons.Trophy,
                )
            }

            val dailyText =
                if (state.isDailyChallengeCompleted) {
                    stringResource(Res.string.daily_challenge_completed)
                } else {
                    stringResource(Res.string.daily_challenge)
                }

            PokerButton(
                text = dailyText,
                onClick = onDailyChallengeClick,
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = AppIcons.DateRange,
                containerColor = if (state.isDailyChallengeCompleted) PokerTheme.OakWood else PokerTheme.ChipRed,
                contentColor = if (state.isDailyChallengeCompleted) PokerTheme.Gold else Color.White,
            )
        }
    }
}
