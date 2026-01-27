package io.github.smithjustinn.services

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission

class AndroidHapticsServiceImpl(
    private val context: Context,
) : HapticsService {
    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateMatch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            vibrate(longArrayOf(0, MATCH_DURATION), intArrayOf(0, MAX_AMPLITUDE))
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateMismatch() {
        vibrate(
            longArrayOf(0, MISMATCH_DURATION, PAUSE_DURATION, MISMATCH_DURATION),
            intArrayOf(0, MAX_AMPLITUDE, 0, MAX_AMPLITUDE),
        )
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            vibrate(longArrayOf(0, TICK_DURATION), intArrayOf(0, TICK_AMPLITUDE))
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            vibrate(longArrayOf(0, WARNING_DURATION), intArrayOf(0, MAX_AMPLITUDE))
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateHeat() {
        // Pronounced heat mode vibration - longer and more intense
        vibrate(
            longArrayOf(0, HEAT_DURATION, PAUSE_DURATION, HEAT_DURATION),
            intArrayOf(0, MAX_AMPLITUDE, 0, MAX_AMPLITUDE),
        )
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate(
        timings: LongArray,
        amplitudes: IntArray,
    ) {
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    companion object {
        private const val MAX_AMPLITUDE = 255
        private const val PAUSE_DURATION = 50L
        private const val MATCH_DURATION = 50L
        private const val MISMATCH_DURATION = 100L
        private const val TICK_DURATION = 10L
        private const val TICK_AMPLITUDE = 150
        private const val WARNING_DURATION = 200L
        private const val HEAT_DURATION = 150L
    }
}
