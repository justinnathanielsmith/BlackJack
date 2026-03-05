package io.github.smithjustinn.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

/**
 * iOS-specific concrete dependency graph.
 * Extends AppGraph and all provider interfaces so Metro can compile the full graph.
 */
@DependencyGraph(AppScope::class)
interface IosAppGraphImpl : AppGraph, CoreProviders, DataProviders, IosProviders

/**
 * Creates the iOS dependency graph using Metro's compile-time graph.
 */
fun createIosGraph(): AppGraph = createGraph<IosAppGraphImpl>()
