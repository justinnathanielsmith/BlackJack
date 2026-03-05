package io.github.smithjustinn.di

import co.touchlab.kermit.Logger
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.utils.CoroutineDispatchers
import io.github.smithjustinn.utils.LoggingContainer

/**
 * Provides core dependencies to the Metro dependency graph.
 * Replaces the old Koin coreModule.
 */
@ContributesTo(AppScope::class)
interface CoreProviders {
    @SingleIn(AppScope::class)
    @Provides
    fun provideLogger(): Logger = LoggingContainer.getLogger()

    @SingleIn(AppScope::class)
    @Provides
    fun provideCoroutineDispatchers(): CoroutineDispatchers = CoroutineDispatchers()
}
