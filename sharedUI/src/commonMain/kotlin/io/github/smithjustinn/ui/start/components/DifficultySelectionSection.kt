package io.github.smithjustinn.ui.start.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.NeonSegmentedControl
import io.github.smithjustinn.ui.start.DifficultyState
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.daily_challenge
import memory_match.sharedui.generated.resources.daily_challenge_completed
import memory_match.sharedui.generated.resources.game_mode
import memory_match.sharedui.generated.resources.leaderboard
import memory_match.sharedui.generated.resources.mode_standard
import memory_match.sharedui.generated.resources.mode_time_attack
import memory_match.sharedui.generated.resources.resume_game
import memory_match.sharedui.generated.resources.select_difficulty
import memory_match.sharedui.generated.resources.settings
import memory_match.sharedui.generated.resources.start
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
            verticalArrangement = Arrangement.spacedBy(32.dp),
        ) {
            // Difficulty Selector
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.select_difficulty),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )

                NeonSegmentedControl(
                    items = state.difficulties,
                    selectedItem = state.selectedDifficulty,
                    onItemSelected = onDifficultySelected,
                    labelProvider = { level -> level.pairs.toString() },
                )
            }

            // Game Mode Selector
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(Res.string.game_mode),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                )

                NeonSegmentedControl(
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

        Spacer(modifier = Modifier.height(48.dp))

        // Step 5: Implement Action Buttons
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Main Button(s)
            if (state.hasSavedGame) {
                NeonStyleButton(
                    text = stringResource(Res.string.resume_game),
                    onClick = onResumeGame,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = AppIcons.ArrowBack,
                )

                NeonStyleButton(
                    text = stringResource(Res.string.start),
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth(),
                    isPrimary = false,
                )
            } else {
                NeonStyleButton(
                    text = stringResource(Res.string.start),
                    onClick = onStartGame,
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            // Secondary Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                NeonStyleButton(
                    text = stringResource(Res.string.settings),
                    onClick = onSettingsClick,
                    modifier = Modifier.weight(1f),
                    isPrimary = false,
                    leadingIcon = AppIcons.Settings,
                )
                NeonStyleButton(
                    text = stringResource(Res.string.leaderboard),
                    onClick = onStatsClick,
                    modifier = Modifier.weight(1f),
                    isPrimary = false,
                    leadingIcon = AppIcons.Trophy,
                )
            }
            // Daily Challenge Button
            val dailyText = if (state.isDailyChallengeCompleted) {
                stringResource(Res.string.daily_challenge_completed)
            } else {
                stringResource(Res.string.daily_challenge)
            }

            NeonStyleButton(
                text = dailyText,
                onClick = onDailyChallengeClick,
                modifier = Modifier.fillMaxWidth(),
                isPrimary = !state.isDailyChallengeCompleted,
                leadingIcon = AppIcons.DateRange, // Need to ensure AppIcons has DateRange or use a fallback
            )
        }
    }
}

@Composable
fun NeonStyleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    trailingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        label = "buttonScale",
    )

    val backgroundColor = if (isPrimary) NeonCyan else InactiveBackground
    val buttonShape = RoundedCornerShape(12.dp)

    Surface(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = modifier
            .height(60.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (isPrimary) {
                    Modifier.shadow(
                        elevation = 12.dp,
                        shape = buttonShape,
                        ambientColor = NeonCyan.copy(alpha = 0.6f),
                        spotColor = NeonCyan.copy(alpha = 0.6f),
                    )
                } else {
                    Modifier
                },
            ),
        shape = buttonShape,
        color = backgroundColor,
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 12.dp),
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    softWrap = false,
                    overflow = TextOverflow.Ellipsis,
                )

                if (trailingIcon != null) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = 145f },
                    )
                }
            }
        }
    }
}
