import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.smithjustinn.App
import io.github.smithjustinn.di.createIosGraph
import io.github.smithjustinn.ui.root.DefaultRootComponent
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle

fun MainViewController(): UIViewController =
    ComposeUIViewController {
        val appGraph = remember { createIosGraph() }
        val lifecycle = remember { LifecycleRegistry() }
        val root =
            remember {
                DefaultRootComponent(
                    componentContext = DefaultComponentContext(lifecycle = lifecycle),
                    appGraph = appGraph,
                )
            }

        appGraph.logger.i { "Logging initialized via Koin" }

        App(
            root = root,
            appGraph = appGraph,
            onThemeChanged = { ApplySystemBarTheme(it) },
        )
    }

@Composable
private fun ApplySystemBarTheme(isDark: Boolean) {
    LaunchedEffect(isDark) {
        UIApplication.sharedApplication.setStatusBarStyle(
            if (isDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent,
        )
    }
}
