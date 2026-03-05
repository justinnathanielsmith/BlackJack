package io.github.smithjustinn.di

import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

/**
 * JVM/Desktop-specific concrete dependency graph.
 * Extends AppGraph (for accessor properties) and all provider interfaces
 * so Metro can discover their @Provides methods at compile time.
 */
@DependencyGraph(AppScope::class)
interface JvmAppGraphImpl : AppGraph, CoreProviders, DataProviders, JvmProviders

/**
 * Creates the JVM/Desktop dependency graph using Metro's compile-time graph.
 */
fun createJvmGraph(): AppGraph = createGraph<JvmAppGraphImpl>()
