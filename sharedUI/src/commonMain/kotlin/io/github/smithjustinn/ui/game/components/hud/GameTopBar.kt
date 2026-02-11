package io.github.smithjustinn.ui.game.components.hud

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.mute_content_description
import io.github.smithjustinn.resources.restart_content_description
import io.github.smithjustinn.resources.score_caps
import io.github.smithjustinn.resources.unmute_content_description
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import org.jetbrains.compose.resources.stringResource

@Composable
fun GameTopBar(
    state: GameTopBarState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMuteClick: () -> Unit,
    onScorePositioned: (Offset) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    GlassDashboard(
        modifier = modifier.statusBarsPadding(),
        isHeatMode = state.isHeatMode,
    ) {
        val isTimeAttack = state.mode == GameMode.TIME_ATTACK

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (state.compact) PokerTheme.spacing.medium else PokerTheme.spacing.large,
                        vertical = if (state.compact) PokerTheme.spacing.extraSmall else PokerTheme.spacing.small,
                    ),
            verticalArrangement =
                Arrangement.spacedBy(
                    if (state.compact) PokerTheme.spacing.extraSmall else PokerTheme.spacing.small,
                ),
        ) {
            TopBarMainRow(
                state = state,
                onBackClick = onBackClick,
                onRestartClick = onRestartClick,
                onMuteClick = onMuteClick,
                onScorePositioned = onScorePositioned,
            )

            if (isTimeAttack && state.maxTime > 0) {
                TimeProgressBar(
                    time = state.time,
                    maxTime = state.maxTime,
                    isLowTime = state.isLowTime,
                    compact = state.compact,
                )
            }
        }
    }
}

@Composable
private fun TopBarMainRow(
    state: GameTopBarState,
    onBackClick: () -> Unit,
    onRestartClick: () -> Unit,
    onMuteClick: () -> Unit,
    onScorePositioned: (Offset) -> Unit,
) {
    Box(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Left: Back Button
        BackButton(
            onClick = onBackClick,
            compact = state.compact,
            modifier = Modifier.align(Alignment.CenterStart),
        )

        // Center: Score
        ScoringDisplay(
            score = state.bankedScore,
            compact = state.compact,
            modifier =
                remember(onScorePositioned) {
                    // Bolt: Prevent recreating Modifier.onGloballyPositioned on every recomposition
                    Modifier
                        .align(Alignment.Center)
                        .onGloballyPositioned { coords ->
                            onScorePositioned(coords.positionInRoot())
                        }
                },
        )

        // Right: Controls
        ControlButtons(
            isAudioEnabled = state.isAudioEnabled,
            onMuteClick = onMuteClick,
            onRestartClick = onRestartClick,
            compact = state.compact,
            showRestart = true,
            modifier = Modifier.align(Alignment.CenterEnd),
        )
    }
}

@Composable
private fun ControlButtons(
    isAudioEnabled: Boolean,
    onMuteClick: () -> Unit,
    onRestartClick: () -> Unit,
    compact: Boolean,
    showRestart: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        MuteButton(
            isAudioEnabled = isAudioEnabled,
            onClick = onMuteClick,
            compact = compact,
        )

        if (showRestart) {
            RestartButton(
                onClick = onRestartClick,
                compact = compact,
            )
        }
    }
}

@Composable
private fun ScoringDisplay(
    score: Int,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    // Current Score Display
    ValueBadge(
        label = stringResource(Res.string.score_caps),
        value = score,
        color = PokerTheme.colors.goldenYellow,
        compact = compact,
        modifier = modifier,
    )
}

@Composable
private fun ValueBadge(
    label: String,
    value: Int,
    color: Color,
    compact: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape =
            RoundedCornerShape(
                topStart = 0.dp,
                topEnd = 0.dp,
                bottomStart = 4.dp,
                bottomEnd = 4.dp,
            ),
        color = color.copy(alpha = 0.15f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier.offset(y = if (compact) 0.dp else (-4).dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = if (compact) 8.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(if (compact) 4.dp else 6.dp),
        ) {
            Text(
                text = label,
                style =
                    typography.labelSmall.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = if (compact) 9.sp else 11.sp,
                        letterSpacing = 0.5.sp,
                        fontFamily =
                            FontWeight.Bold.toString().lowercase().let {
                                FontFamily.Serif
                            },
                    ),
                color = color.copy(alpha = 0.8f),
            )

            Text(
                text = value.toString(),
                style =
                    typography.titleMedium.copy(
                        fontSize = if (compact) 14.sp else 18.sp,
                        fontWeight = FontWeight.Black,
                        fontFamily = FontFamily.Serif,
                    ),
                color = color,
            )
        }
    }
}

@Composable
private fun BackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val size = if (compact) 40.dp else 48.dp

    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Surface(
            shape = CircleShape,
            color = PokerTheme.colors.tacticalRed,
            shadowElevation = PokerTheme.spacing.small,
            modifier = Modifier.size(size),
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Dashed border effect (simplified for icon button)
                Canvas(modifier = Modifier.matchParentSize()) {
                    val strokeWidth = size.toPx() * STROKE_WIDTH_FACTOR
                    drawCircle(
                        color = Color.White,
                        style =
                            Stroke(
                                width = strokeWidth,
                                pathEffect =
                                    PathEffect
                                        .dashPathEffect(floatArrayOf(DASH_ON, DASH_OFF), 0f),
                            ),
                        radius = this.size.minDimension / 2 * RADIUS_FACTOR,
                    )
                }

                Icon(
                    AppIcons.ArrowBack,
                    contentDescription = stringResource(Res.string.back_content_description),
                    tint = PokerTheme.colors.goldenYellow,
                    modifier = Modifier.size(if (compact) 20.dp else 24.dp),
                )
            }
        }
    }
}

@Composable
private fun RestartButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Surface(
            shape = PokerTheme.shapes.medium,
            color = PokerTheme.colors.oakWood,
            shadowElevation = PokerTheme.spacing.extraSmall,
            modifier = Modifier.size(if (compact) 40.dp else 48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    AppIcons.Restart,
                    contentDescription = stringResource(Res.string.restart_content_description),
                    tint = PokerTheme.colors.goldenYellow,
                    modifier = Modifier.size(if (compact) 20.dp else 24.dp),
                )
            }
        }
    }
}

@Composable
private fun MuteButton(
    isAudioEnabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
    ) {
        Surface(
            shape = PokerTheme.shapes.medium,
            color = PokerTheme.colors.oakWood,
            shadowElevation = PokerTheme.spacing.extraSmall,
            modifier = Modifier.size(if (compact) 40.dp else 48.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    if (isAudioEnabled) AppIcons.VolumeUp else AppIcons.VolumeOff,
                    contentDescription =
                        stringResource(
                            if (isAudioEnabled) {
                                Res.string.mute_content_description
                            } else {
                                Res.string.unmute_content_description
                            },
                        ),
                    tint = if (isAudioEnabled) PokerTheme.colors.goldenYellow else PokerTheme.colors.tacticalRed,
                    modifier = Modifier.size(if (compact) 20.dp else 24.dp),
                )
            }
        }
    }
}

@Composable
private fun TimeProgressBar(
    time: Long,
    maxTime: Long,
    isLowTime: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
) {
    val progress by animateFloatAsState(
        targetValue = (time.toFloat() / maxTime.toFloat()).coerceIn(0f, 1f),
        animationSpec = spring(stiffness = Spring.StiffnessLow),
    )

    Box(
        modifier =
            modifier
                .fillMaxWidth()
                .height(if (compact) 4.dp else 8.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = BG_ALPHA)),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .shadow(
                        elevation = if (isLowTime) PokerTheme.spacing.none else PokerTheme.spacing.medium,
                        shape = CircleShape,
                        ambientColor = PokerTheme.colors.goldenYellow,
                        spotColor = PokerTheme.colors.goldenYellow,
                        clip = false,
                    ).clip(CircleShape)
                    .background(
                        if (isLowTime) {
                            Brush.horizontalGradient(
                                listOf(PokerTheme.colors.tacticalRed, PokerTheme.colors.tacticalRed.copy(alpha = 0.7f)),
                            )
                        } else {
                            Brush.horizontalGradient(
                                listOf(
                                    PokerTheme.colors.goldenYellow,
                                    PokerTheme.colors.goldenYellow.copy(alpha = 0.7f),
                                ),
                            )
                        },
                    ),
        )
    }
}

private const val STROKE_WIDTH_FACTOR = 0.1f
private const val DASH_ON = 10f
private const val DASH_OFF = 10f
private const val RADIUS_FACTOR = 0.85f
private const val BG_ALPHA = 0.4f
