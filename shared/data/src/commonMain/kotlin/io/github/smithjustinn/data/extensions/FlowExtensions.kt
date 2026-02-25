package io.github.smithjustinn.data.extensions

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

internal fun <T, R> Flow<T>.mapToStateFlow(
    scope: CoroutineScope,
    initialValue: R,
    started: SharingStarted = SharingStarted.Eagerly,
    transform: suspend (T) -> R,
): StateFlow<R> = map(transform).stateIn(scope, started, initialValue)
