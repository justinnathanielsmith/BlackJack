package io.github.smithjustinn.ui.start

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.resources.Res
import io.github.smithjustinn.resources.clean_number_format
import io.github.smithjustinn.resources.debug
import io.github.smithjustinn.resources.settings
import io.github.smithjustinn.resources.shop_title
import io.github.smithjustinn.resources.stats
import io.github.smithjustinn.resources.wallet_label
import io.github.smithjustinn.services.AudioService
import io.github.smithjustinn.services.HapticFeedbackType
import io.github.smithjustinn.theme.PokerTheme
import io.github.smithjustinn.ui.components.AppCard
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.components.AuroraEffect
import io.github.smithjustinn.ui.components.ShopIcons
import io.github.smithjustinn.ui.components.pokerBackground
import io.github.smithjustinn.ui.components.rememberGlimmerBrush
import io.github.smithjustinn.ui.start.components.DifficultySelectionSection
import io.github.smithjustinn.ui.start.components.StartHeader
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource

@Composable
fun StartContent(
    component: StartComponent,
    modifier: Modifier = Modifier,
) {
    val state by component.state.collectAsState()
    val graph = LocalAppGraph.current
    val audioService = graph.audioService

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .pokerBackground(),
    ) {
        AuroraEffect(
            modifier = Modifier.align(Alignment.BottomCenter),
        )

        StartScreenLayout(
            state = state,
            shouldAnimateEntrance = state.shouldAnimateEntrance,
            onAnimationComplete = component::onEntranceAnimationCompleted,
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
            onShopClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onShopClick()
            },
            onDebugClick = {
                audioService.playEffect(AudioService.SoundEffect.CLICK)
                component.onDebugClick()
            },
            modifier = Modifier.fillMaxSize(),
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
private const val HEADER_ANIMATION_DURATION = 800
private const val CONTENT_ANIMATION_DURATION = 600
private const val CONTENT_ANIMATION_DELAY = 200
private const val CONTENT_INITIAL_OFFSET_Y = 100f

@Composable
private fun StartScreenLayout(
    state: DifficultyState,
    shouldAnimateEntrance: Boolean,
    onAnimationComplete: () -> Unit,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    onSettingsClick: () -> Unit,
    onStatsClick: () -> Unit,
    onShopClick: () -> Unit,
    onDebugClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Animation States
    val animations = rememberEntranceAnimations(shouldAnimateEntrance, onAnimationComplete)
    val headerAlpha = animations.headerAlpha
    val contentAlpha = animations.contentAlpha
    val contentOffsetY = animations.contentOffsetY

    Box(
        modifier =
            modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
    ) {
        val headerAlphaValue = headerAlpha.value
        val headerModifier = Modifier.graphicsLayer { alpha = headerAlphaValue }

        StartTopActions(
            onStatsClick = onStatsClick,
            onSettingsClick = onSettingsClick,
            onShopClick = onShopClick,
            onDebugClick = onDebugClick,
            totalBalance = state.totalBalance,
            modifier = headerModifier,
        )

        StartMainContent(
            state = state,
            onDifficultySelected = onDifficultySelected,
            onModeSelected = onModeSelected,
            onStartGame = onStartGame,
            onResumeGame = onResumeGame,
            modifier =
                Modifier
                    .graphicsLayer {
                        alpha = contentAlpha.value
                        translationY = contentOffsetY.value
                    },
        )
    }
}

@Composable
private fun BoxScope.StartTopActions(
    onStatsClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit,
    onDebugClick: () -> Unit,
    totalBalance: Long,
    modifier: Modifier = Modifier,
) {
    val spacing = PokerTheme.spacing

    // Top Start Action Row
    Row(
        modifier =
            modifier
                .align(Alignment.TopStart)
                .padding(spacing.medium),
    ) {
        MedallionIcon(
            icon = ShopIcons.ShoppingCart,
            onClick = onShopClick,
            applyGlimmer = true,
            tint = PokerTheme.colors.goldenYellow,
            contentDescription = stringResource(Res.string.shop_title),
        )

        WalletBadge(
            amount = totalBalance,
            modifier = Modifier.padding(start = spacing.small),
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
            applyGlimmer = true,
            tint = PokerTheme.colors.silver,
            contentDescription = stringResource(Res.string.stats),
        )
        MedallionIcon(
            icon = AppIcons.Settings,
            onClick = onSettingsClick,
            applyGlimmer = true,
            contentDescription = stringResource(Res.string.settings),
        )
        MedallionIcon(
            icon = AppIcons.VisibilityOff,
            onClick = onDebugClick,
            applyGlimmer = false,
            tint = Color.Red,
            contentDescription = stringResource(Res.string.debug),
        )
    }
}

@Composable
private fun StartMainContent(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = PokerTheme.spacing

    Column(
        modifier =
            modifier
                .fillMaxSize()
                .padding(horizontal = spacing.large),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Clear the TopActionRow height
        Spacer(modifier = Modifier.height(START_HEADER_SPACER_HEIGHT_DP.dp))

        Spacer(modifier = Modifier.weight(1f))

        StartHeader(
            backTheme = state.cardBackTheme,
            symbolTheme = state.cardSymbolTheme,
            areSuitsMultiColored = state.areSuitsMultiColored,
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

        Spacer(modifier = Modifier.height(spacing.medium))

        Spacer(modifier = Modifier.weight(1f))

        Spacer(modifier = Modifier.height(spacing.large))
    }
}

@Composable
private fun MedallionIcon(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.Black.copy(alpha = MEDALLION_BG_ALPHA),
    tint: Color = PokerTheme.colors.goldenYellow,
    applyGlimmer: Boolean = false,
    contentDescription: String? = null,
) {
    val colors = PokerTheme.colors
    val hapticsService = LocalAppGraph.current.hapticsService
    val glimmerBrush = if (applyGlimmer) rememberGlimmerBrush() else null

    Surface(
        onClick = {
            hapticsService.performHapticFeedback(HapticFeedbackType.LIGHT)
            onClick()
        },
        shape = CircleShape,
        color = backgroundColor,
        border =
            BorderStroke(
                MEDALLION_BORDER_WIDTH_DP.dp,
                colors.goldenYellow.copy(alpha = MEDALLION_BORDER_ALPHA),
            ),
        modifier = modifier.size(MEDALLION_SIZE_DP.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = if (applyGlimmer) Color.White else tint,
                modifier =
                    Modifier
                        .size(MEDALLION_ICON_SIZE_DP.dp)
                        .then(
                            if (applyGlimmer && glimmerBrush != null) {
                                Modifier
                                    .graphicsLayer(alpha = 0.99f)
                                    .drawWithCache {
                                        onDrawWithContent {
                                            drawContent()
                                            drawRect(brush = glimmerBrush, blendMode = BlendMode.SrcIn)
                                        }
                                    }
                            } else {
                                Modifier
                            },
                        ),
            )
        }
    }
}

@Composable
private fun WalletBadge(
    amount: Long,
    modifier: Modifier = Modifier,
) {
    val walletLabel = stringResource(Res.string.wallet_label)
    val formattedAmount = stringResource(Res.string.clean_number_format, amount)

    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = MEDALLION_BG_ALPHA),
        border =
            BorderStroke(
                MEDALLION_BORDER_WIDTH_DP.dp,
                PokerTheme.colors.goldenYellow.copy(alpha = MEDALLION_BORDER_ALPHA),
            ),
        modifier =
            modifier
                .height(MEDALLION_SIZE_DP.dp)
                .semantics {
                    contentDescription = "$walletLabel: $formattedAmount"
                },
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp),
        ) {
            Icon(
                imageVector = ShopIcons.CasinoChip,
                contentDescription = null,
                tint = PokerTheme.colors.goldenYellow,
                modifier = Modifier.size(MEDALLION_ICON_SIZE_DP.dp),
            )

            Text(
                text = formattedAmount,
                style = MaterialTheme.typography.titleMedium,
                color = PokerTheme.colors.goldenYellow,
            )
        }
    }
}

@Composable
private fun rememberEntranceAnimations(
    shouldAnimate: Boolean,
    onAnimationComplete: () -> Unit,
): StartEntranceAnimations {
    val headerAlpha = remember { Animatable(if (shouldAnimate) 0f else 1f) }
    val contentAlpha = remember { Animatable(if (shouldAnimate) 0f else 1f) }
    val contentOffsetY = remember { Animatable(if (shouldAnimate) CONTENT_INITIAL_OFFSET_Y else 0f) }

    LaunchedEffect(shouldAnimate, onAnimationComplete) {
        if (shouldAnimate) {
            coroutineScope {
                // Sequence animations
                headerAlpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = HEADER_ANIMATION_DURATION, easing = LinearOutSlowInEasing),
                )

                launch {
                    contentAlpha.animateTo(
                        targetValue = 1f,
                        animationSpec =
                            tween(
                                durationMillis = CONTENT_ANIMATION_DURATION,
                                delayMillis = CONTENT_ANIMATION_DELAY,
                            ),
                    )
                }
                launch {
                    contentOffsetY.animateTo(
                        targetValue = 0f,
                        animationSpec =
                            spring(
                                dampingRatio = Spring.DampingRatioLowBouncy,
                                stiffness = Spring.StiffnessLow,
                            ),
                    )
                }
            }
            onAnimationComplete()
        }
    }
    return StartEntranceAnimations(headerAlpha, contentAlpha, contentOffsetY)
}

private data class StartEntranceAnimations(
    val headerAlpha: Animatable<Float, AnimationVector1D>,
    val contentAlpha: Animatable<Float, AnimationVector1D>,
    val contentOffsetY: Animatable<Float, AnimationVector1D>,
)
