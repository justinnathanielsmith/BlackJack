package io.github.smithjustinn.ui.game.components.cards

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runComposeUiTest
import androidx.compose.ui.unit.dp
import kotlin.test.Test

class CardShadowLayerTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testCardShadowLayerRendersWithoutCrash() =
        runComposeUiTest {
            val elevationState = mutableStateOf(0.dp)
            val yOffsetState = mutableStateOf(0.dp)

            setContent {
                CardShadowLayer(
                    elevation = elevationState,
                    yOffset = yOffsetState,
                    isRecentlyMatched = false,
                )
            }

            elevationState.value = 10.dp
            waitForIdle()

            yOffsetState.value = 5.dp
            waitForIdle()
        }
}
