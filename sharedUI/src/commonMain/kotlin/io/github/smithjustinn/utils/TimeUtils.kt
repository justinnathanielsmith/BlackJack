package io.github.smithjustinn.utils

import kotlin.time.Duration.Companion.seconds

/**
 * Formats a duration in seconds into a MM:SS string using Kotlin Duration.
 */
fun formatTime(seconds: Long): String =
    seconds.seconds.toComponents { minutes, secs, _ ->
        "${minutes.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
    }
