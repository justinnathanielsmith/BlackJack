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

fun main() {
    val lifecycle = LifecycleRegistry()
    val appGraph = createJvmGraph()
    val root = DefaultRootComponent(
        componentContext = DefaultComponentContext(lifecycle = lifecycle),
        appGraph = appGraph
    )

    application {
        val windowState = rememberWindowState(
            position = WindowPosition(Alignment.Center),
            size = DpSize(1100.dp, 850.dp),
            placement = WindowPlacement.Floating
        )
        
        Window(
            title = "Memory Match",
            state = windowState,
            onCloseRequest = ::exitApplication,
        ) {
            window.minimumSize = Dimension(900, 700)
            appGraph.logger.i { "Logging initialized via Metro" }
            App(
                root = root,
                appGraph = appGraph
            )
        }
    }
}
