package io.github.smithjustinn.services

import dev.zacsweers.metro.Inject

@Inject
class JvmHapticsServiceImpl : HapticsService {
    override fun vibrateMatch() {
        // No-op for Desktop
    }

    override fun vibrateMismatch() {
        // No-op for Desktop
    }

    override fun vibrateTick() {
        // No-op for Desktop
    }

    override fun vibrateWarning() {
        // No-op for Desktop
    }

    override fun vibrateHeat() {
        // No-op for Desktop
    }
}
