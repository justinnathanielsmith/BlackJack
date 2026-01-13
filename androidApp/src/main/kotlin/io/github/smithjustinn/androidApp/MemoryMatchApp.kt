package io.github.smithjustinn.androidApp

import android.app.Application
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.createAndroidGraph

class MemoryMatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        appGraph.logger.i { "Logging initialized via Metro with Kermit" }
    }

    val appGraph: AppGraph by lazy {
        createAndroidGraph(this)
    }
}
