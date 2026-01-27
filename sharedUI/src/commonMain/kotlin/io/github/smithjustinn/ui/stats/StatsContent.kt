package io.github.smithjustinn.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.high_scores
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.stats.components.LeaderboardSection
import io.github.smithjustinn.ui.stats.components.ModeSelector
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsContent(
    component: StatsComponent,
    modifier: Modifier = Modifier,
) {
    val graph = LocalAppGraph.current
    val state by component.state.collectAsState()
    val audioService = graph.audioService
    val colors = PokerTheme.colors

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            when (event) {
                StatsUiEvent.PlayClick -> audioService.playEffect(AudioService.SoundEffect.CLICK)
            }
        }
    }

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
                            radius = 1000f,
                        ),
                ),
    ) {
        StatsMainContent(state, component, audioService)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsMainContent(
    state: StatsState,
    component: StatsComponent,
    audioService: AudioService,
) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            StatsTopBar(audioService, component)
        },
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize()
                        .widthIn(max = 600.dp)
                        .align(Alignment.TopCenter),
            ) {
                ModeSelector(
                    selectedMode = state.selectedGameMode,
                    onModeSelected = {
                        audioService.playEffect(AudioService.SoundEffect.CLICK)
                        component.onGameModeSelected(it)
                    },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = PokerTheme.spacing.large, vertical = PokerTheme.spacing.small),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding =
                        PaddingValues(
                            horizontal = PokerTheme.spacing.large,
                            vertical = PokerTheme.spacing.medium,
                        ),
                    verticalArrangement = Arrangement.spacedBy(PokerTheme.spacing.small),
                ) {
                    items(state.difficultyLeaderboards) { (level, entries) ->
                        LeaderboardSection(level, entries)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StatsTopBar(
    audioService: AudioService,
    component: StatsComponent,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.high_scores),
                style =
                    MaterialTheme.typography.titleLarge.copy(
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    ),
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        navigationIcon = {
            IconButton(onClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onBack()
            }) {
                Icon(
                    imageVector = AppIcons.ArrowBack,
                    contentDescription = stringResource(Res.string.back_content_description),
                    tint = PokerTheme.colors.goldenYellow,
                )
            }
        },
    )
}
