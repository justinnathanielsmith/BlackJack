package io.github.smithjustinn.domain

import io.github.smithjustinn.domain.models.MemoryGameState
import io.github.smithjustinn.utils.CoroutineDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.sample
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlin.time.Clock

/**
 * Manages the persistence of game state with an optimized strategy:
 * - High priority updates (e.g., moves) are saved immediately.
 * - Normal priority updates (e.g., timer ticks) are debounced to reduce I/O.
 * - Ensures the latest state is always saved on flush or session end.
 */
class GameStateSyncManager(
    private val scope: CoroutineScope,
    private val dispatchers: CoroutineDispatchers,
    private val onSave: (MemoryGameState, Long) -> Unit,
) {
    private val syncChannel = Channel<SyncRequest>(Channel.CONFLATED)
    private val mutex = Mutex()
    private var lastSavedState: MemoryGameState? = null
    private var lastSavedTime: Long? = null
    private var lastProcessedRequestTime: Long = 0L

    init {
        scope.launch(dispatchers.default) {
            @OptIn(FlowPreview::class)
            syncChannel
                .receiveAsFlow()
                .sample(DEBOUNCE_DELAY_MS)
                .collect { request ->
                    mutex.withLock {
                        performSave(request.state, request.time, request.queuedAt)
                    }
                }
        }
    }

    /**
     * Schedules a sync request.
     * @param priority Use [Priority.HIGH] for critical events like matches/moves.
     */
    fun sync(
        state: MemoryGameState,
        time: Long,
        priority: Priority = Priority.NORMAL,
    ) {
        if (priority == Priority.HIGH) {
            // Immediate save for high priority
            val now = Clock.System.now().toEpochMilliseconds()
            scope.launch(dispatchers.mainImmediate) {
                mutex.withLock {
                    performSave(state, time, now)
                }
            }
        } else {
            // Conflated channel ensures we only keep the latest normal priority request
            syncChannel.trySend(SyncRequest(state, time))
        }
    }

    private fun performSave(
        state: MemoryGameState,
        time: Long,
        requestTimestamp: Long,
    ) {
        if (requestTimestamp < lastProcessedRequestTime) return
        lastProcessedRequestTime = requestTimestamp

        if (state == lastSavedState && time == lastSavedTime) return

        onSave(state, time)
        lastSavedState = state
        lastSavedTime = time
    }

    /**
     * Force-saves the current state if it hasn't been saved yet.
     */
    suspend fun flush(
        state: MemoryGameState,
        time: Long,
    ) {
        val now = Clock.System.now().toEpochMilliseconds()
        mutex.withLock {
            performSave(state, time, now)
        }
    }

    enum class Priority {
        NORMAL,
        HIGH,
    }

    private data class SyncRequest(
        val state: MemoryGameState,
        val time: Long,
        val queuedAt: Long = Clock.System.now().toEpochMilliseconds(),
    )

    companion object {
        private const val DEBOUNCE_DELAY_MS = 2000L
    }
}
