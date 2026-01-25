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
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.stats.components.LeaderboardSection
import io.github.smithjustinn.ui.stats.components.ModeSelector
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.back_content_description
import memory_match.sharedui.generated.resources.high_scores
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
                        Brush.verticalGradient(
                            colors = listOf(StartBackgroundTop, StartBackgroundBottom),
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
                        .widthIn(max = 800.dp)
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
                            .padding(horizontal = 24.dp, vertical = 12.dp),
                )

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp),
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
                style = MaterialTheme.typography.titleLarge,
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
                    tint = Color.White,
                )
            }
        },
    )
}
