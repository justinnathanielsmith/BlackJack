package io.github.smithjustinn.ui.start

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.start.components.DifficultySelectionSection
import io.github.smithjustinn.ui.start.components.StartHeader

@Composable
fun StartContent(
    component: StartComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val graph = LocalAppGraph.current
    val audioService = graph.audioService
    val colors = PokerTheme.colors

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(
                    brush =
                        Brush.radialGradient(
                            colors =
                                listOf(
                                    colors.feltGreen,
                                    colors.feltGreenDark,
                                ),
                            center = androidx.compose.ui.geometry.Offset.Unspecified,
                            radius = Float.POSITIVE_INFINITY, // Smoother radial falloff
                        ),
                ),
    ) {
        StartScreenLayout(
            state = state,
            onDifficultySelected = { level ->
                audioService.playEffect(AudioService.SoundEffect.PLINK)
                component.onDifficultySelected(level)
            },
            onModeSelected = { mode ->
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onModeSelected(mode)
            },
            onStartGame = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onStartGame()
            },
            onResumeGame = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onResumeGame()
            },
            onSettingsClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onSettingsClick()
            },
            onStatsClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onStatsClick()
            },
            onDailyChallengeClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onDailyChallengeClick()
            },
            modifier = Modifier.fillMaxSize(), // Fill so we can align icons to top-right
        )
    }
}

private const val MEDALLION_SIZE_DP = 48
private const val MEDALLION_ICON_SIZE_DP = 24
private const val MEDALLION_BORDER_WIDTH_DP = 1
private const val MEDALLION_BORDER_ALPHA = 0.5f
private const val MEDALLION_BG_ALPHA = 0.4f
private const val START_HEADER_SPACER_HEIGHT_DP = 64
private const val DEALER_TRAY_MAX_WIDTH_DP = 600
private const val MAIN_CONTENT_BOTTOM_SPACER_WEIGHT = 0.5f
private const val RADIAL_FALLOFF_RADIUS = Float.POSITIVE_INFINITY

@Composable
private fun StartScreenLayout(
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
    val spacing = PokerTheme.spacing

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        StartTopActions(
            state = state,
            onDailyChallengeClick = onDailyChallengeClick,
            onStatsClick = onStatsClick,
            onSettingsClick = onSettingsClick,
        )

        StartMainContent(
            state = state,
            onDifficultySelected = onDifficultySelected,
            onModeSelected = onModeSelected,
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
        )
    }
}

@Composable
private fun BoxScope.StartTopActions(
    state: DifficultyState,
    onDailyChallengeClick: () -> Unit,
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
) {
    val spacing = PokerTheme.spacing

    // Top Start Action Row (Daily Challenge)
    Row(
        modifier =
            Modifier
                .align(Alignment.TopStart)
                .padding(spacing.medium),
    ) {
        MedallionIcon(
            icon = AppIcons.DateRange,
            onClick = onDailyChallengeClick,
            backgroundColor =
                if (state.isDailyChallengeCompleted) {
                    PokerTheme.colors.oakWood
                } else {
                    Color.Black.copy(alpha = MEDALLION_BG_ALPHA)
                },
            tint = if (state.isDailyChallengeCompleted) PokerTheme.colors.goldenYellow else Color.White,
        )
    }

    // Top End Action Row
    Row(
        modifier =
            Modifier
                .align(Alignment.TopEnd)
                .padding(spacing.medium),
        horizontalArrangement = Arrangement.spacedBy(spacing.small),
    ) {
        MedallionIcon(
            icon = AppIcons.Trophy,
            onClick = onStatsClick,
        )
        MedallionIcon(
            icon = AppIcons.Settings,
            onClick = onSettingsClick,
        )
    }
}

@Composable
private fun BoxScope.StartMainContent(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
) {
    val spacing = PokerTheme.spacing

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Clear the TopActionRow height
        Spacer(modifier = Modifier.height(START_HEADER_SPACER_HEIGHT_DP.dp))

        Spacer(modifier = Modifier.weight(1f))

        StartHeader(
            settings = state.cardSettings,
            modifier = Modifier.padding(bottom = spacing.large),
        )

        Spacer(modifier = Modifier.weight(MAIN_CONTENT_BOTTOM_SPACER_WEIGHT))

        // Dealer's Tray Container
        AppCard(
            modifier = Modifier.widthIn(max = DEALER_TRAY_MAX_WIDTH_DP.dp),
        ) {
            DifficultySelectionSection(
                state = state,
                onDifficultySelected = onDifficultySelected,
                onModeSelected = onModeSelected,
                onStartGame = onStartGame,
                onResumeGame = onResumeGame,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(spacing.large))
    }
}

@Composable
private fun MedallionIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = MEDALLION_BG_ALPHA),
    tint: Color = PokerTheme.colors.goldenYellow,
) {
    val colors = PokerTheme.colors

    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = backgroundColor,
        border =
            androidx.compose.foundation.BorderStroke(
                MEDALLION_BORDER_WIDTH_DP.dp,
                colors.goldenYellow.copy(alpha = MEDALLION_BORDER_ALPHA),
            ),
        modifier = modifier.size(MEDALLION_SIZE_DP.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(MEDALLION_ICON_SIZE_DP.dp),
            )
        }
    }
}
