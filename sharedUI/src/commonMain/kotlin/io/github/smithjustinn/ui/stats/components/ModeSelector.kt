package io.github.smithjustinn.ui.stats.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.smithjustinn.domain.models.GameMode
import io.github.smithjustinn.ui.components.NeonSegmentedControl
import memory_match.sharedui.generated.resources.*
import org.jetbrains.compose.resources.stringResource

@Composable
fun ModeSelector(
    selectedMode: GameMode,
    onModeSelected: (GameMode) -> Unit,
    modifier: Modifier = Modifier
) {
    val modes = listOf(GameMode.STANDARD, GameMode.TIME_ATTACK)
    
    NeonSegmentedControl(
        items = modes,
        selectedItem = selectedMode,
        onItemSelected = onModeSelected,
        labelProvider = { mode ->
            when (mode) {
                GameMode.STANDARD -> stringResource(Res.string.mode_standard)
                GameMode.TIME_ATTACK -> stringResource(Res.string.mode_time_attack)
            }
        },
        modifier = modifier
    )
}
