import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import io.github.smithjustinn.App
import io.github.smithjustinn.di.createJvmGraph
import io.github.smithjustinn.ui.root.DefaultRootComponent
import java.awt.Dimension
import javax.swing.SwingUtilities

fun main() {
    lateinit var lifecycle: LifecycleRegistry
    lateinit var root: DefaultRootComponent
    lateinit var appGraph: io.github.smithjustinn.di.AppGraph

    SwingUtilities.invokeAndWait {
        lifecycle = LifecycleRegistry()
        appGraph = createJvmGraph()
        root =
            DefaultRootComponent(
                componentContext = DefaultComponentContext(lifecycle = lifecycle),
                appGraph = appGraph,
            )
    }

    application {
        val windowState =
            rememberWindowState(
                position = WindowPosition(Alignment.Center),
                size = DpSize(WINDOW_WIDTH.dp, WINDOW_HEIGHT.dp),
                placement = WindowPlacement.Floating,
            )

        Window(
            title = "Memory Match",
            state = windowState,
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(MIN_WINDOW_WIDTH, MIN_WINDOW_HEIGHT)
            appGraph.logger.i { "Logging initialized via Metro" }
            App(
                root = root,
                appGraph = appGraph,
            )
        }
    }
}

private const val WINDOW_WIDTH = 1100
private const val WINDOW_HEIGHT = 850
private const val MIN_WINDOW_WIDTH = 900
private const val MIN_WINDOW_HEIGHT = 700
