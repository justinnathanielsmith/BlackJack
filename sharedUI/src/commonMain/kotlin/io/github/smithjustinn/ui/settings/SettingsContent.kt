package io.github.smithjustinn.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.back_content_description
import io.github.smithjustinn.resources.settings
import io.github.smithjustinn.resources.settings_appearance
import io.github.smithjustinn.resources.settings_enable_heat_shield
import io.github.smithjustinn.resources.settings_enable_heat_shield_desc
import io.github.smithjustinn.resources.settings_enable_peek
import io.github.smithjustinn.resources.settings_enable_peek_desc
import io.github.smithjustinn.resources.settings_enable_third_eye
import io.github.smithjustinn.resources.settings_enable_third_eye_desc
import io.github.smithjustinn.resources.settings_four_color_deck
import io.github.smithjustinn.resources.settings_four_color_deck_desc
import io.github.smithjustinn.resources.settings_game_music
import io.github.smithjustinn.resources.settings_game_music_desc
import io.github.smithjustinn.resources.settings_gameplay_audio
import io.github.smithjustinn.resources.settings_percentage_format
import io.github.smithjustinn.resources.settings_reset
import io.github.smithjustinn.resources.settings_reset_walkthrough
import io.github.smithjustinn.resources.settings_reset_walkthrough_desc
import io.github.smithjustinn.resources.settings_sound_effects
import io.github.smithjustinn.resources.settings_sound_effects_desc
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.theme.ModernGold
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.pokerBackground
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
        component.events.collect { event: SettingsUiEvent ->
            when (event) {
                SettingsUiEvent.PlayClick -> audioService.playEffect(AudioService.SoundEffect.CLICK)
            }
        }
    }

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground(),
    ) {
        AuroraEffect(
            modifier = Modifier.align(Alignment.BottomCenter),
        )

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
                            .padding(horizontal = PokerTheme.spacing.large, vertical = PokerTheme.spacing.medium)
                            .widthIn(max = 600.dp)
                            .align(Alignment.TopCenter),
                    verticalArrangement = Arrangement.spacedBy(20.dp),
                ) {
                    SettingsAudioSection(state, audioService, component)
                    SettingsAppearanceSection(state, audioService, component)
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
                        fontFamily = FontFamily.Serif,
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
                    tint = ModernGold,
                )
            }
        },
    )
}

@Composable
private fun SettingsAudioSection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    AppCard(title = stringResource(Res.string.settings_gameplay_audio)) {
        val soundEffectsTitle = stringResource(Res.string.settings_sound_effects)
        SettingsToggle(
            title = soundEffectsTitle,
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
                label = soundEffectsTitle,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = PokerTheme.spacing.small),
            color = Color.White.copy(alpha = 0.1f),
        )

        val gameMusicTitle = stringResource(Res.string.settings_game_music)
        SettingsToggle(
            title = gameMusicTitle,
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
                label = gameMusicTitle,
            )
        }

        HorizontalDivider(
            modifier = Modifier.padding(vertical = PokerTheme.spacing.small),
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
private fun SettingsAppearanceSection(
    state: SettingsState,
    audioService: AudioService,
    component: SettingsComponent,
) {
    if (state.isFourColorUnlocked || state.isThirdEyeUnlocked || state.isHeatShieldUnlocked) {
        AppCard(title = stringResource(Res.string.settings_appearance)) {
            if (state.isFourColorUnlocked) {
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

            if (state.isFourColorUnlocked && state.isThirdEyeUnlocked) {
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = PokerTheme.spacing.small),
                    color = Color.White.copy(alpha = 0.1f),
                )
            }

            if (state.isThirdEyeUnlocked) {
                SettingsToggle(
                    title = stringResource(Res.string.settings_enable_third_eye),
                    description = stringResource(Res.string.settings_enable_third_eye_desc),
                    checked = state.isThirdEyeEnabled,
                    onCheckedChange = {
                        audioService.playEffect(AudioService.SoundEffect.CLICK)
                        component.toggleThirdEyeEnabled(it)
                    },
                )
            }

            if (state.isHeatShieldUnlocked) {
                if (state.isFourColorUnlocked || state.isThirdEyeUnlocked) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = PokerTheme.spacing.small),
                        color = Color.White.copy(alpha = 0.1f),
                    )
                }

                SettingsToggle(
                    title = stringResource(Res.string.settings_enable_heat_shield),
                    description = stringResource(Res.string.settings_enable_heat_shield_desc),
                    checked = state.isHeatShieldEnabled,
                    onCheckedChange = {
                        audioService.playEffect(AudioService.SoundEffect.CLICK)
                        component.toggleHeatShieldEnabled(it)
                    },
                )
            }
        }
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
            horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
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
                            ModernGold
                                .copy(alpha = 0.2f),
                        contentColor = ModernGold,
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
private fun VolumeSlider(
    value: Float,
    onValueChange: (Float) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
) {
    val volumeIcon =
        when {
            value == 0f -> AppIcons.VolumeOff
            value <= 0.5f -> AppIcons.VolumeDown
            else -> AppIcons.VolumeUp
        }

    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
    ) {
        Icon(
            imageVector = volumeIcon,
            contentDescription = null,
            tint = ModernGold,
            modifier = Modifier.size(18.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier =
                Modifier
                    .weight(1f)
                    .semantics { contentDescription = label },
            colors =
                SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = ModernGold,
                    inactiveTrackColor = PokerTheme.colors.hudBackground,
                ),
        )
        Text(
            text = stringResource(Res.string.settings_percentage_format, (value * 100).toInt()),
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
        modifier =
            modifier
                .fillMaxWidth()
                .toggleable(
                    value = checked,
                    onValueChange = onCheckedChange,
                    role = Role.Switch,
                ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(PokerTheme.spacing.medium),
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
            onCheckedChange = null,
            modifier = Modifier.clearAndSetSemantics {},
            colors =
                SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = ModernGold,
                    uncheckedTrackColor = PokerTheme.colors.hudBackground,
                    uncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    uncheckedBorderColor = Color.Transparent,
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = ModernGold,
                    disabledUncheckedTrackColor = PokerTheme.colors.hudBackground,
                    disabledUncheckedThumbColor = Color.White.copy(alpha = 0.6f),
                    disabledUncheckedBorderColor = Color.Transparent,
                ),
        )
    }
}
