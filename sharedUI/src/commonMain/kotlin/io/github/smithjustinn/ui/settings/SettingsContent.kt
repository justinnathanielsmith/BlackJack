package io.github.smithjustinn.ui.settings

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
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
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.settings
import io.github.smithjustinn.resources.settings_appearance
import io.github.smithjustinn.resources.settings_card_back_style
import io.github.smithjustinn.resources.settings_enable_peek
import io.github.smithjustinn.resources.settings_enable_peek_desc
import io.github.smithjustinn.resources.settings_four_color_deck
import io.github.smithjustinn.resources.settings_four_color_deck_desc
import io.github.smithjustinn.resources.settings_game_music
import io.github.smithjustinn.resources.settings_game_music_desc
import io.github.smithjustinn.resources.settings_gameplay_audio
import io.github.smithjustinn.resources.settings_reset
import io.github.smithjustinn.resources.settings_reset_walkthrough
import io.github.smithjustinn.resources.settings_reset_walkthrough_desc
import io.github.smithjustinn.resources.settings_sound_effects
import io.github.smithjustinn.resources.settings_sound_effects_desc
import io.github.smithjustinn.resources.settings_symbol_style
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.PillSegmentedControl
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsContent(
    component: SettingsComponent,
    modifier: Modifier = Modifier,
) {
    val graph = LocalAppGraph.current
    val state by component.state.collectAsState()
    val audioService = graph.audioService

    LaunchedEffect(Unit) {
        component.events.collect { event ->
            when (event) {
                SettingsUiEvent.PlayClick -> audioService.playEffect(AudioService.SoundEffect.CLICK)
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
                                    io.github.smithjustinn.theme.FeltGreenTop,
                                    io.github.smithjustinn.theme.FeltGreenBottom,
                                ),
                            center = androidx.compose.ui.geometry.Offset.Unspecified,
                            radius = 1000f, // Broad spread for the "tabletop" look
                        ),
                ),
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = { SettingsTopBar(audioService, component) },
        ) { padding ->
            Box(modifier = Modifier.fillMaxSize().padding(padding)) {
                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 16.dp)
                            .widthIn(max = 600.dp)
                            .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SettingsAppearanceSection(state, audioService, component)
                    SettingsAudioSection(state, audioService, component)
                    SettingsResetSection(state, audioService, component)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    audioService: AudioService,
    component: SettingsComponent,
) {
    TopAppBar(
        title = {
            Text(
                text = stringResource(Res.string.settings),
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
                    tint = io.github.smithjustinn.theme.GoldenYellow,
                )
            }
        },
    )
}

@Composable
private fun SettingsAppearanceSection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    AppCard(title = stringResource(Res.string.settings_appearance)) {
        ThemeSelector(
            title = stringResource(Res.string.settings_card_back_style),
            options = CardBackTheme.entries,
            selected = state.cardBackTheme,
            onSelect = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.setCardBackTheme(it)
            },
            labelProvider = { it.name.lowercase().replaceFirstChar { char -> char.uppercase() } },
        )

        Spacer(modifier = Modifier.height(16.dp))

        ThemeSelector(
            title = stringResource(Res.string.settings_symbol_style),
            options = CardSymbolTheme.entries,
            selected = state.cardSymbolTheme,
            onSelect = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.setCardSymbolTheme(it)
            },
            labelProvider = {
                it.name.lowercase().replace("text_only", "text only").replaceFirstChar { char ->
                    char.uppercase()
                }
            },
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 12.dp),
            color = Color.White.copy(alpha = 0.1f),
        )

        SettingsToggle(
            title = stringResource(Res.string.settings_four_color_deck),
            description = stringResource(Res.string.settings_four_color_deck_desc),
            checked = state.areSuitsMultiColored,
            onCheckedChange = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.toggleSuitsMultiColored(it)
            },
        )
    }
}

@Composable
private fun SettingsAudioSection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    AppCard(title = stringResource(Res.string.settings_gameplay_audio)) {
        SettingsToggle(
            title = stringResource(Res.string.settings_sound_effects),
            description = stringResource(Res.string.settings_sound_effects_desc),
            checked = state.isSoundEnabled,
            onCheckedChange = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.toggleSoundEnabled(it)
            },
        )

        if (state.isSoundEnabled) {
            VolumeSlider(
                value = state.soundVolume,
                onValueChange = { component.setSoundVolume(it) },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.1f),
        )

        SettingsToggle(
            title = stringResource(Res.string.settings_game_music),
            description = stringResource(Res.string.settings_game_music_desc),
            checked = state.isMusicEnabled,
            onCheckedChange = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.toggleMusicEnabled(it)
            },
        )

        if (state.isMusicEnabled) {
            VolumeSlider(
                value = state.musicVolume,
                onValueChange = { component.setMusicVolume(it) },
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            color = Color.White.copy(alpha = 0.1f),
        )

        SettingsToggle(
            title = stringResource(Res.string.settings_enable_peek),
            description = stringResource(Res.string.settings_enable_peek_desc),
            checked = state.isPeekEnabled,
            onCheckedChange = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.togglePeekEnabled(it)
            },
        )
    }
}

@Composable
private fun SettingsResetSection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    AppCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(Res.string.settings_reset_walkthrough),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(Res.string.settings_reset_walkthrough_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    lineHeight = 16.sp,
                )
            }
            Button(
                onClick = {
                    audioService.playEffect(AudioService.SoundEffect.CLICK)
                    component.resetWalkthrough()
                },
                enabled = state.isWalkthroughCompleted,
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor =
                            io.github.smithjustinn.theme.GoldenYellow
                                .copy(alpha = 0.2f),
                        contentColor = io.github.smithjustinn.theme.GoldenYellow,
                        disabledContainerColor = PokerTheme.colors.hudBackground.copy(alpha = 0.5f),
                        disabledContentColor = Color.White.copy(alpha = 0.3f),
                    ),
            ) {
                Text(stringResource(Res.string.settings_reset))
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
    labelProvider: @Composable (T) -> String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )

        PillSegmentedControl(
            items = options,
            selectedItem = selected,
            onItemSelected = onSelect,
            labelProvider = { labelProvider(it) },
        )
    }
}

@Composable
private fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = AppIcons.VolumeUp,
            contentDescription = null,
            tint = io.github.smithjustinn.theme.GoldenYellow,
            modifier = Modifier.size(18.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors =
                SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = io.github.smithjustinn.theme.GoldenYellow,
                    inactiveTrackColor = PokerTheme.colors.hudBackground,
                ),
        )
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelMedium,
            color = Color.White.copy(alpha = 0.6f),
            modifier = Modifier.width(32.dp),
        )
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.6f),
                lineHeight = 16.sp,
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = io.github.smithjustinn.theme.GoldenYellow,
                    uncheckedTrackColor = PokerTheme.colors.hudBackground,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    uncheckedBorderColor = Color.Transparent,
                ),
        )
    }
}
