import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.compose.ui.window.WindowPlacement
import java.awt.Dimension
import io.github.smithjustinn.App
import io.github.smithjustinn.di.createJvmGraph

fun main() = application {
    val windowState = rememberWindowState(
        placement = WindowPlacement.Floating
    )
    
    Window(
        title = "Memory Match",
        state = windowState,
        onCloseRequest = ::exitApplication,
    ) {
        window.minimumSize = Dimension(800, 600)
        val appGraph = remember { createJvmGraph() }
        appGraph.logger.i { "Logging initialized via Metro" }
        App(appGraph = appGraph)
    }
}
