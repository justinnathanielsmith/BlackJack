package io.github.smithjustinn.di

import android.content.Context
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.createGraphFactory

/**
 * Android-specific concrete dependency graph.
 * Extends AppGraph and all provider interfaces so Metro can compile the full graph.
 * Uses a Factory to accept runtime inputs (Context and Activity).
 */
@DependencyGraph(AppScope::class)
interface AndroidAppGraphImpl : AppGraph, CoreProviders, DataProviders, AndroidProviders {

    /**
     * Factory for creating the Android dependency graph with runtime inputs.
     * Context is provided as a dependency that Android providers can use.
     */
    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides context: Context,
            @Provides activity: android.app.Activity?,
        ): AndroidAppGraphImpl
    }
}

/**
 * Creates the Android dependency graph using Metro's compile-time graph factory.
 */
fun createAndroidGraph(application: android.app.Application): AppGraph =
    createGraphFactory<AndroidAppGraphImpl.Factory>()
        .create(
            context = application as Context,
            activity = null,
        )
