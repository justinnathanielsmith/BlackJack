package io.github.smithjustinn.ui.root

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.arkivanov.decompose.extensions.compose.stack.animation.fade
import com.arkivanov.decompose.extensions.compose.stack.animation.plus
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.predictiveBackAnimation
import com.arkivanov.decompose.extensions.compose.stack.animation.slide
import com.arkivanov.decompose.extensions.compose.stack.animation.stackAnimation
import io.github.smithjustinn.ui.debug.DebugContent
import io.github.smithjustinn.ui.game.GameContent
import io.github.smithjustinn.ui.settings.SettingsContent
import io.github.smithjustinn.ui.shop.ShopContent
import io.github.smithjustinn.ui.start.StartContent
import io.github.smithjustinn.ui.stats.StatsContent

@OptIn(ExperimentalDecomposeApi::class)
@Composable
fun RootContent(
    component: RootComponent,
    modifier: Modifier = Modifier,
) {
    Children(
        stack = component.childStack,
        modifier = modifier,
        animation =
            predictiveBackAnimation(
                backHandler = component.backHandler,
                fallbackAnimation = stackAnimation(slide() + fade()),
                onBack = component::pop,
            ),
    ) {
        when (val child = it.instance) {
            is RootComponent.Child.Start -> StartContent(child.component)
            is RootComponent.Child.Game -> GameContent(child.component)
            is RootComponent.Child.Settings -> SettingsContent(child.component)
            is RootComponent.Child.Stats -> StatsContent(child.component)
            is RootComponent.Child.Shop -> ShopContent(child.component)
            is RootComponent.Child.Debug -> DebugContent(child.component)
        }
    }
}
