package io.github.smithjustinn.di

import android.app.Application
import android.content.pm.ApplicationInfo
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger

/**
 * Android-specific implementation of the dependency graph.
 * Refactored to use Koin for initialization and dependency management.
 */
fun createAndroidGraph(application: Application): AppGraph {
    initKoin(androidUiModule) {
        if (0 != application.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) {
            androidLogger()
        }
        androidContext(application)
    }
    return KoinAppGraph()
}
