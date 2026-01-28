package io.github.smithjustinn.ui.start.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.smithjustinn.domain.models.CardDisplaySettings

/**
 * StartHeader
 */
@Composable
fun StartHeader(
    modifier: Modifier = Modifier,
    settings: CardDisplaySettings = CardDisplaySettings(),
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CardPreview(
            modifier = Modifier.height(180.dp),
            settings = settings,
        )
    }
}


