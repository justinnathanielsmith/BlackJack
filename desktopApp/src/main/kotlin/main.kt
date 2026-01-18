import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import java.awt.Dimension
import io.github.smithjustinn.App
import io.github.smithjustinn.di.createJvmGraph

fun main() = application {
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
        val appGraph = remember { createJvmGraph() }
        appGraph.logger.i { "Logging initialized via Metro" }
        App(appGraph = appGraph)
    }
}
