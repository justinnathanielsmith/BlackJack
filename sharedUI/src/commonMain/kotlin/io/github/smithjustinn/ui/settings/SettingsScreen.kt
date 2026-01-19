package io.github.smithjustinn.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.ui.components.AppIcons
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.back_content_description
import memory_match.sharedui.generated.resources.settings
import org.jetbrains.compose.resources.stringResource

class SettingsScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val graph = LocalAppGraph.current
        val screenModel = rememberScreenModel { graph.settingsScreenModel }
        val state by screenModel.state.collectAsState()
        val navigator = LocalNavigator.currentOrThrow
        val audioService = graph.audioService

        LaunchedEffect(Unit) {
            screenModel.events.collect { event ->
                when (event) {
                    SettingsUiEvent.PlayClick -> audioService.playClick()
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = {
                            Text(
                                text = stringResource(Res.string.settings),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
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
                                    contentDescription = stringResource(Res.string.back_content_description),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .widthIn(max = 600.dp)
                            .align(Alignment.TopCenter),
                        verticalArrangement = Arrangement.spacedBy(20.dp)
                    ) {
                        SettingsCard(title = "Appearance") {
                            ThemeSelector(
                                title = "Card Back Style",
                                options = CardBackTheme.entries,
                                selected = state.cardBackTheme,
                                onSelect = { 
                                    audioService.playClick()
                                    screenModel.setCardBackTheme(it) 
                                },
                                labelProvider = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            ThemeSelector(
                                title = "Symbol Style",
                                options = CardSymbolTheme.entries,
                                selected = state.cardSymbolTheme,
                                onSelect = { 
                                    audioService.playClick()
                                    screenModel.setCardSymbolTheme(it) 
                                },
                                labelProvider = { it.name.lowercase().replace("text_only", "text only").replaceFirstChar { char -> char.uppercase() } }
                            )

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 12.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            SettingsToggle(
                                title = "Four Color Deck",
                                description = "Use different colors for each suit (Hearts Red, Diamonds Blue, Clubs Green, Spades Black)",
                                checked = state.areSuitsMultiColored,
                                onCheckedChange = {
                                    audioService.playClick()
                                    screenModel.toggleSuitsMultiColored(it)
                                }
                            )
                        }

                        SettingsCard(title = "Gameplay & Audio") {
                            SettingsToggle(
                                title = "Sound Effects",
                                description = "Play sounds for card flips, matches, and wins",
                                checked = state.isSoundEnabled,
                                onCheckedChange = { 
                                    audioService.playClick()
                                    screenModel.toggleSoundEnabled(it) 
                                }
                            )

                            if (state.isSoundEnabled) {
                                VolumeSlider(
                                    value = state.soundVolume,
                                    onValueChange = { screenModel.setSoundVolume(it) }
                                )
                            }
                            
                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            SettingsToggle(
                                title = "Game Music",
                                description = "Play background music during gameplay",
                                checked = state.isMusicEnabled,
                                onCheckedChange = { 
                                    audioService.playClick()
                                    screenModel.toggleMusicEnabled(it) 
                                }
                            )

                            if (state.isMusicEnabled) {
                                VolumeSlider(
                                    value = state.musicVolume,
                                    onValueChange = { screenModel.setMusicVolume(it) }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                            )

                            SettingsToggle(
                                title = "Enable Memory Peek",
                                description = "Show cards briefly at the start of the game",
                                checked = state.isPeekEnabled,
                                onCheckedChange = { 
                                    audioService.playClick()
                                    screenModel.togglePeekEnabled(it) 
                                }
                            )
                        }

                        SettingsCard {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Reset Walkthrough",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Show the tutorial again on the next game",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        lineHeight = 16.sp
                                    )
                                }
                                Button(
                                    onClick = {
                                        audioService.playClick()
                                        screenModel.resetWalkthrough()
                                    },
                                    enabled = state.isWalkthroughCompleted,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                ) {
                                    Text("Reset")
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun <T> ThemeSelector(
        title: String,
        options: List<T>,
        selected: T,
        onSelect: (T) -> Unit,
        labelProvider: (T) -> String
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEach { option ->
                    val isSelected = option == selected
                    val containerColor = if (isSelected) 
                        MaterialTheme.colorScheme.primaryContainer 
                    else 
                        MaterialTheme.colorScheme.surface
                    
                    val contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimaryContainer
                    else
                        MaterialTheme.colorScheme.onSurface
                    
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSelect(option) },
                        color = containerColor,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                        )
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = labelProvider(option),
                                style = MaterialTheme.typography.labelMedium,
                                color = contentColor,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun VolumeSlider(
        value: Float,
        onValueChange: (Float) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = AppIcons.VolumeUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Slider(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            Text(
                text = "${(value * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.width(32.dp)
            )
        }
    }

    @Composable
    private fun SettingsCard(
        title: String? = null,
        content: @Composable ColumnScope.() -> Unit
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                if (title != null) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                content()
            }
        }
    }

    @Composable
    private fun SettingsToggle(
        title: String,
        description: String,
        checked: Boolean,
        onCheckedChange: (Boolean) -> Unit,
        modifier: Modifier = Modifier
    ) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = 16.sp
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
