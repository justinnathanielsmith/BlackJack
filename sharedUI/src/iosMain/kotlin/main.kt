import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.window.ComposeUIViewController
import io.github.smithjustinn.App
import io.github.smithjustinn.di.createIosGraph
import platform.UIKit.UIApplication
import platform.UIKit.UIStatusBarStyleDarkContent
import platform.UIKit.UIStatusBarStyleLightContent
import platform.UIKit.UIViewController
import platform.UIKit.setStatusBarStyle
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.smithjustinn.ui.root.DefaultRootComponent

fun MainViewController(): UIViewController = ComposeUIViewController { 
    val appGraph = remember { createIosGraph() }
    val lifecycle = remember { LifecycleRegistry() }
    val root = remember { 
        DefaultRootComponent(
            componentContext = DefaultComponentContext(lifecycle = lifecycle),
            appGraph = appGraph
        )
    }
    
    appGraph.logger.i { "Logging initialized via Metro" }
    
    App(
        root = root,
        appGraph = appGraph,
        onThemeChanged = { ThemeChanged(it) }
    )
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
    LaunchedEffect(isDark) {
        UIApplication.sharedApplication.setStatusBarStyle(
            if (isDark) UIStatusBarStyleDarkContent else UIStatusBarStyleLightContent
        )
    }
}
