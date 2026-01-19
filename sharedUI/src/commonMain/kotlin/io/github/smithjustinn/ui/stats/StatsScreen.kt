package io.github.smithjustinn.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
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
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.platform.JavaSerializable
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.stats.components.LeaderboardSection
import io.github.smithjustinn.ui.stats.components.ModeSelector
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

class StatsScreen : Screen, JavaSerializable {
    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.statsScreenModel }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val audioService = graph.audioService

        LaunchedEffect(Unit) {
            screenModel.events.collect { event ->
                when (event) {
                    StatsUiEvent.PlayClick -> audioService.playClick()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(StartBackgroundTop, StartBackgroundBottom)
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { 
                            Text(
                                text = stringResource(Res.string.high_scores),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            ) 
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                        navigationIcon = {
                            IconButton(onClick = { 
                                audioService.playClick()
                                navigator.pop() 
                            }) {
                                Icon(
                                    imageVector = AppIcons.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 800.dp)
                            .align(Alignment.TopCenter)
                    ) {
                        ModeSelector(
                            selectedMode = state.selectedGameMode,
                            onModeSelected = { 
                                audioService.playClick()
                                screenModel.onGameModeSelected(it) 
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 12.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(24.dp)
                        ) {
                            items(state.difficultyLeaderboards) { (level, entries) ->
                                LeaderboardSection(level, entries)
                            }
                        }
                    }
                }
            }
        }
    }
}
