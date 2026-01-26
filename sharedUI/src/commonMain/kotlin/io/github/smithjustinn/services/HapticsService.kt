package io.github.smithjustinn.services

interface HapticsService {
    fun vibrateMatch()

    fun vibrateMismatch()

    fun vibrateTick()

    fun vibrateWarning()

    fun vibrateHeat()
}
