package io.github.smithjustinn.utils

import co.touchlab.kermit.Logger
import co.touchlab.kermit.NoTagFormatter
import co.touchlab.kermit.loggerConfigInit
import co.touchlab.kermit.platformLogWriter
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import io.github.smithjustinn.di.AppScope

@BindingContainer
@ContributesTo(AppScope::class)
object LoggingContainer {
    @Provides
    @SingleIn(AppScope::class)
    fun provideLogger(): Logger = Logger(loggerConfigInit(platformLogWriter(NoTagFormatter)))
}
