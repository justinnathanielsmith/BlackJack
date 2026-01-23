package io.github.smithjustinn.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.CardBackTheme
import io.github.smithjustinn.domain.models.CardSymbolTheme
import io.github.smithjustinn.theme.InactiveBackground
import io.github.smithjustinn.theme.NeonCyan
import io.github.smithjustinn.theme.StartBackgroundBottom
import io.github.smithjustinn.theme.StartBackgroundTop
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.NeonSegmentedControl
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    component: SettingsComponent,
    modifier: Modifier = Modifier
) {
    val graph = LocalAppGraph.current
    val state by component.state.collectAsState()
    val audioService = graph.audioService

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            when (event) {
                SettingsUiEvent.PlayClick -> audioService.playClick()
            }
        }
    }

    Box(
        modifier = modifier
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
                            text = stringResource(Res.string.settings),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = { 
                            audioService.playClick()
                            component.onBack() 
                        }) {
                            Icon(
                                imageVector = AppIcons.ArrowBack,
                                contentDescription = stringResource(Res.string.back_content_description),
                                tint = Color.White
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
                    SettingsCard(title = stringResource(Res.string.settings_appearance)) {
                        ThemeSelector(
                            title = stringResource(Res.string.settings_card_back_style),
                            options = CardBackTheme.entries,
                            selected = state.cardBackTheme,
                            onSelect = { 
                                audioService.playClick()
                                component.setCardBackTheme(it) 
                            },
                            labelProvider = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        ThemeSelector(
                            title = stringResource(Res.string.settings_symbol_style),
                            options = CardSymbolTheme.entries,
                            selected = state.cardSymbolTheme,
                            onSelect = { 
                                audioService.playClick()
                                component.setCardSymbolTheme(it) 
                            },
                            labelProvider = { it.name.lowercase().replace("text_only", "text only").replaceFirstChar { char -> char.uppercase() } }
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        )

                        SettingsToggle(
                            title = stringResource(Res.string.settings_four_color_deck),
                            description = stringResource(Res.string.settings_four_color_deck_desc),
                            checked = state.areSuitsMultiColored,
                            onCheckedChange = {
                                audioService.playClick()
                                component.toggleSuitsMultiColored(it)
                            }
                        )
                    }

                    SettingsCard(title = stringResource(Res.string.settings_gameplay_audio)) {
                        SettingsToggle(
                            title = stringResource(Res.string.settings_sound_effects),
                            description = stringResource(Res.string.settings_sound_effects_desc),
                            checked = state.isSoundEnabled,
                            onCheckedChange = { 
                                audioService.playClick()
                                component.toggleSoundEnabled(it) 
                            }
                        )

                        if (state.isSoundEnabled) {
                            VolumeSlider(
                                value = state.soundVolume,
                                onValueChange = { component.setSoundVolume(it) }
                            )
                        }
                        
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        )

                        SettingsToggle(
                            title = stringResource(Res.string.settings_game_music),
                            description = stringResource(Res.string.settings_game_music_desc),
                            checked = state.isMusicEnabled,
                            onCheckedChange = { 
                                audioService.playClick()
                                component.toggleMusicEnabled(it) 
                            }
                        )

                        if (state.isMusicEnabled) {
                            VolumeSlider(
                                value = state.musicVolume,
                                onValueChange = { component.setMusicVolume(it) }
                            )
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.White.copy(alpha = 0.1f)
                        )

                        SettingsToggle(
                            title = stringResource(Res.string.settings_enable_peek),
                            description = stringResource(Res.string.settings_enable_peek_desc),
                            checked = state.isPeekEnabled,
                            onCheckedChange = { 
                                audioService.playClick()
                                component.togglePeekEnabled(it) 
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
                                    text = stringResource(Res.string.settings_reset_walkthrough),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Text(
                                    text = stringResource(Res.string.settings_reset_walkthrough_desc),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.6f),
                                    lineHeight = 16.sp
                                )
                            }
                            Button(
                                onClick = {
                                    audioService.playClick()
                                    component.resetWalkthrough()
                                },
                                enabled = state.isWalkthroughCompleted,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan.copy(alpha = 0.2f),
                                    contentColor = NeonCyan,
                                    disabledContainerColor = InactiveBackground.copy(alpha = 0.5f),
                                    disabledContentColor = Color.White.copy(alpha = 0.3f)
                                )
                            ) {
                                Text(stringResource(Res.string.settings_reset))
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
    labelProvider: @Composable (T) -> String
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        NeonSegmentedControl(
            items = options,
            selectedItem = selected,
            onItemSelected = onSelect,
            labelProvider = { labelProvider(it) }
        )
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
            tint = NeonCyan,
            modifier = Modifier.size(18.dp)
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = Color.White,
                activeTrackColor = NeonCyan,
                inactiveTrackColor = InactiveBackground
            )
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
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
        color = InactiveBackground.copy(alpha = 0.4f),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.1f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            if (title != null) {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.ExtraBold,
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
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 16.sp
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = NeonCyan,
                uncheckedTrackColor = InactiveBackground,
                uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                uncheckedBorderColor = Color.Transparent
            )
        )
    }
}
