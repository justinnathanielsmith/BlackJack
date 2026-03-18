package io.github.smithjustinn.di

import dev.zacsweers.metro.DependencyGraph

/**
 * JVM/Desktop-specific concrete dependency graph.
 * Extends AppGraph (for accessor properties) and all provider interfaces
 * so Metro can discover their @Provides methods at compile time.
 */
@DependencyGraph(AppScope::class)
interface JvmAppGraph :
    AppGraph,
    CoreProviders,
    DataProviders,
    JvmUiModule {
    @dev.zacsweers.metro.DependencyGraph.Factory
    interface Factory {
        fun create(): JvmAppGraph
    }
}

/**
 * Creates the JVM/Desktop dependency graph using Metro's compile-time graph factory.
 */
fun createJvmGraph(): AppGraph =
    dev.zacsweers.metro
        .createGraphFactory<JvmAppGraph.Factory>()
        .create()
