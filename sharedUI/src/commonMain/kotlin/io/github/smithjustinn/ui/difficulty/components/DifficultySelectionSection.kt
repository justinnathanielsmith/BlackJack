package io.github.smithjustinn.ui.difficulty.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.smithjustinn.domain.models.DifficultyLevel
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.AppIcons
import io.github.smithjustinn.ui.difficulty.DifficultyState
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun DifficultySelectionSection(
    state: DifficultyState,
    onDifficultySelected: (DifficultyLevel) -> Unit,
    onModeSelected: (GameMode) -> Unit,
    onStartGame: () -> Unit,
    onResumeGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Card with Entrance Animation
        var visible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) { visible = true }

        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600)) + expandVertically(tween(600, easing = EaseOutBack))
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFF0F1E3D).copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(vertical = 24.dp, horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(Res.string.how_many_pairs),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Pulsing Trophy Icon
                        val infiniteTransition = rememberInfiniteTransition(label = "trophyGlow")
                        val trophyScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1500, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "trophyScale"
                        )
                        
                        Icon(
                            imageVector = AppIcons.Trophy,
                            contentDescription = null,
                            tint = Color(0xFFFFD700).copy(alpha = 0.7f),
                            modifier = Modifier.size(20.dp).graphicsLayer {
                                scaleX = trophyScale
                                scaleY = trophyScale
                            }
                        )
                    }

                    CircularDifficultySelector(
                        difficulties = state.difficulties,
                        selectedDifficulty = state.selectedDifficulty,
                        onDifficultySelected = onDifficultySelected
                    )
                }
            }
        }

        // Game Mode with Delayed Entrance
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 200)) + slideInVertically(tween(600, delayMillis = 200)) { it / 2 }
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFF0F1E3D).copy(alpha = 0.8f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF1E3A8A))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(Res.string.game_mode),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )

                    GameModeSwitch(
                        selectedMode = state.selectedMode,
                        onModeSelected = onModeSelected
                    )
                }
            }
        }

        // Action Buttons
        Spacer(Modifier.height(8.dp))
        
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(tween(600, delayMillis = 400)) + scaleIn(tween(600, delayMillis = 400), initialScale = 0.8f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (state.hasSavedGame) {
                    // Resume Button
                    StartButton(
                        text = stringResource(Res.string.resume_game),
                        onClick = onResumeGame,
                        gradientColors = listOf(Color(0xFF22C55E), Color(0xFF15803D)),
                        borderColor = Color(0xFF4ADE80)
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // New Game Button
                    OutlinedButton(
                        onClick = onStartGame,
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(48.dp),
                        shape = RoundedCornerShape(24.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF60A5FA)),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF60A5FA)
                        )
                    ) {
                        Text(
                            text = stringResource(Res.string.start),
                            fontWeight = FontWeight.Bold
                        )
                    }
                } else {
                    // Start Button
                    StartButton(
                        text = stringResource(Res.string.start),
                        onClick = onStartGame,
                        gradientColors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF)),
                        borderColor = Color(0xFFFFD700),
                        textColor = Color(0xFFFFD700),
                        showPlayIcon = true
                    )
                }
            }
        }
    }
}

@Composable
fun StartButton(
    text: String,
    onClick: () -> Unit,
    gradientColors: List<Color>,
    borderColor: Color,
    textColor: Color = Color.White,
    showPlayIcon: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "startButtonScale"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .fillMaxWidth(0.8f)
            .height(64.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .border(2.dp, borderColor, RoundedCornerShape(32.dp)),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent
        ),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.horizontalGradient(gradientColors)),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
                if (showPlayIcon) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        imageVector = AppIcons.ArrowBack, // Used as Play arrow
                        contentDescription = null,
                        tint = textColor,
                        modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = 180f }
                    )
                }
            }
        }
    }
}

@Composable
fun GameModeSwitch(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit
) {
    // Custom Surface acting as a track
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF0A1225)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val width = maxWidth
            val indicatorWidth = width / 2
            
            // Internal Box that animates its offset based on the selected mode
            val indicatorOffset by animateDpAsState(
                targetValue = if (selectedMode == GameMode.STANDARD) 0.dp else indicatorWidth,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
                label = "modeIndicatorOffset"
            )
            
            // The Indicator
            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(indicatorWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF00C6FF), Color(0xFF0072FF))
                        )
                    )
            )

            // Options
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeSelected(GameMode.STANDARD) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.mode_standard),
                        color = if (selectedMode == GameMode.STANDARD) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onModeSelected(GameMode.TIME_ATTACK) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(Res.string.mode_time_attack),
                        color = if (selectedMode == GameMode.TIME_ATTACK) Color.White else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
fun CircularDifficultySelector(
    difficulties: List<DifficultyLevel>,
    selectedDifficulty: DifficultyLevel,
    onDifficultySelected: (DifficultyLevel) -> Unit
) {
    val currentIndex = difficulties.indexOf(selectedDifficulty)
    
    // Animation for the arc
    val sweepAngle = 240f
    val stepAngle = sweepAngle / (difficulties.size - 1)
    val targetSweep = stepAngle * currentIndex
    val animatedSweep by animateFloatAsState(
        targetValue = targetSweep,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "difficultySweep"
    )

    // Number animation for pairs
    val animatedPairs by animateIntAsState(
        targetValue = selectedDifficulty.pairs,
        animationSpec = tween(400, easing = FastOutSlowInEasing),
        label = "pairsAnimation"
    )

    // Glow animation for the arc
    val infiniteTransition = rememberInfiniteTransition(label = "arcGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        // Minus Button
        DifficultyIconButton(
            onClick = { if (currentIndex > 0) onDifficultySelected(difficulties[currentIndex - 1]) },
            enabled = currentIndex > 0,
            text = "-"
        )

        Spacer(Modifier.width(16.dp))

        // Circular Dial
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            val primaryColor = Color(0xFF00C6FF)
            val trackColor = Color(0xFF1E3A8A)
            
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 8.dp.toPx()
                val radius = size.minDimension / 2 - strokeWidth - 5.dp.toPx()
                val startAngle = 150f
                
                // Track
                drawArc(
                    color = trackColor.copy(alpha = 0.2f),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                )
                
                // Active Arc with Glow
                if (animatedSweep > 0) {
                    // Outer Glow
                    drawArc(
                        color = primaryColor.copy(alpha = glowAlpha * 0.3f),
                        startAngle = startAngle,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth * 2f, cap = StrokeCap.Round)
                    )
                    
                    // Main Arc
                    drawArc(
                        color = primaryColor,
                        startAngle = startAngle,
                        sweepAngle = animatedSweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                
                // Handle
                val handleAngleDeg = startAngle + animatedSweep
                val handleAngleRad = (handleAngleDeg * PI / 180f).toFloat()
                val handleX = center.x + radius * cos(handleAngleRad)
                val handleY = center.y + radius * sin(handleAngleRad)
                
                drawCircle(color = Color.White, radius = strokeWidth, center = Offset(handleX, handleY))
                drawCircle(color = primaryColor.copy(alpha = 0.4f), radius = strokeWidth * 1.5f, center = Offset(handleX, handleY))
            }

            // Center Text with Animation
            AnimatedContent(
                targetState = selectedDifficulty,
                transitionSpec = {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) + 
                     scaleIn(initialScale = 0.85f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
                },
                label = "difficultyTextTransition"
            ) { targetDifficulty ->
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(targetDifficulty.nameRes), 
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF60A5FA),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = animatedPairs.toString(),
                        style = MaterialTheme.typography.displayMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = stringResource(Res.string.pairs_label),
                        style = MaterialTheme.typography.titleMedium,
                        color = Color.White
                    )
                }
            }
        }

        Spacer(Modifier.width(16.dp))

        // Plus Button
        DifficultyIconButton(
            onClick = { if (currentIndex < difficulties.size - 1) onDifficultySelected(difficulties[currentIndex + 1]) },
            enabled = currentIndex < difficulties.size - 1,
            text = "+"
        )
    }
}

@Composable
private fun DifficultyIconButton(
    onClick: () -> Unit,
    enabled: Boolean,
    text: String
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
        label = "difficultyIconButtonScale"
    )

    IconButton(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        modifier = Modifier
            .size(48.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .background(
                if (enabled) Brush.verticalGradient(listOf(Color(0xFF00C6FF), Color(0xFF0072FF)))
                else Brush.verticalGradient(listOf(Color.Gray.copy(alpha = 0.3f), Color.Gray.copy(alpha = 0.5f))),
                CircleShape
            )
            .border(2.dp, if (enabled) Color(0xFF1E3A8A) else Color.Gray, CircleShape)
    ) {
        Text(text, color = if (enabled) Color.White else Color.Gray, fontSize = 24.sp, fontWeight = FontWeight.Bold)
    }
}
