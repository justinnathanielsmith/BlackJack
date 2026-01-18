package io.github.smithjustinn.ui.difficulty.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import memory_match.sharedui.generated.resources.Res
import memory_match.sharedui.generated.resources.app_name
import memory_match.sharedui.generated.resources.select_difficulty
import org.jetbrains.compose.resources.stringResource

@Composable
fun DifficultyHeader(
    modifier: Modifier = Modifier,
    scale: Float = 1f
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.app_name),
            style = if (scale < 1f) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center,
            letterSpacing = if (scale < 1f) 1.sp else 2.sp
        )

        Surface(
            color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f),
            shape = CircleShape,
            modifier = Modifier.padding(top = if (scale < 1f) 4.dp else 12.dp)
        ) {
            Text(
                text = stringResource(Res.string.select_difficulty),
                style = if (scale < 1f) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = if (scale < 1f) 4.dp else 6.dp)
            )
        }
    }
}
