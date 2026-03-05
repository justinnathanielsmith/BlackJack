package io.github.smithjustinn.androidApp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import com.arkivanov.decompose.defaultComponentContext
import io.github.smithjustinn.App
import io.github.smithjustinn.ui.root.DeepLinkHandler
import io.github.smithjustinn.ui.root.DefaultRootComponent

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appGraph = (application as MemoryMatchApp).appGraph
        val root =
            DefaultRootComponent(
                componentContext = defaultComponentContext(),
                appGraph = appGraph,
            )

        handleIntent(intent)

        setContent {
            App(
                root = root,
                appGraph = appGraph,
                onThemeChanged = { ApplySystemBarTheme(it) },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        intent?.data?.toString()?.let { url ->
            DeepLinkHandler.handleDeepLink(url)
        }
    }
}

@Composable
private fun ApplySystemBarTheme(isDark: Boolean) {
    val view = LocalView.current
    LaunchedEffect(isDark) {
        val window = (view.context as Activity).window
        WindowInsetsControllerCompat(window, window.decorView).apply {
            // The app uses a dark gradient background (StartBackgroundTop) at the top
            // of most screens (Start, Game, Settings, Stats) regardless of the system theme.
            // Therefore, we should always use light icons (isAppearanceLightStatusBars = false)
            // to ensure they are visible against the dark background.
            isAppearanceLightStatusBars = false
            isAppearanceLightNavigationBars = false
        }
    }
}
