package io.github.smithjustinn.androidApp

import android.app.Application
import com.google.android.gms.ads.MobileAds
import io.github.smithjustinn.di.AppGraph
import io.github.smithjustinn.di.createAndroidGraph

class MemoryMatchApp : Application() {
    override fun onCreate() {
        super.onCreate()
        MobileAds.initialize(this)
        appGraph.logger.i { "Logging initialized via Koin with Kermit" }
    }

    val appGraph: AppGraph by lazy {
        createAndroidGraph(this)
    }
}
