package io.github.smithjustinn.androidApp

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import io.github.smithjustinn.App

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { 
            App(
                appGraph = (application as MemoryMatchApp).appGraph,
                onThemeChanged = { ThemeChanged(it) }
            ) 
        }
    }
}

@Composable
private fun ThemeChanged(isDark: Boolean) {
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
