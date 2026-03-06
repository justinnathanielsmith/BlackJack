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

internal inline fun <T, R> Flow<Iterable<T>>.mapList(crossinline transform: suspend (T) -> R): Flow<List<R>> =
    map { list -> list.map { transform(it) } }

internal inline fun <T, R> Flow<T?>.mapNullable(crossinline transform: suspend (T) -> R): Flow<R?> =
    map { it?.let { transform(it) } }
