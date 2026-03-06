package io.github.smithjustinn.di

import dev.zacsweers.metro.DependencyGraph

/**
 * iOS-specific concrete dependency graph.
 * Extends AppGraph and all provider interfaces so Metro can compile the full graph.
 */
@DependencyGraph(AppScope::class)
interface IosAppGraph :
    AppGraph,
    CoreProviders,
    DataProviders,
    IosUiModule

/**
 * Creates the iOS dependency graph using Metro's compile-time graph factory.
 */
fun createIosGraph(): AppGraph = createGraphFactory<IosAppGraph>().create()
