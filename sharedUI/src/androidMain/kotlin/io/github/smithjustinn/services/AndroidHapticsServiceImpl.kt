package io.github.smithjustinn.services

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.annotation.RequiresPermission
import dev.zacsweers.metro.Inject

@Inject
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
        vibrate(longArrayOf(0, 50), intArrayOf(0, 255))
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateMismatch() {
        vibrate(longArrayOf(0, 100, 50, 100), intArrayOf(0, 255, 0, 255))
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateTick() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
        } else {
            vibrate(longArrayOf(0, 10), intArrayOf(0, 150))
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateWarning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
        } else {
            vibrate(longArrayOf(0, 200), intArrayOf(0, 255))
        }
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    override fun vibrateHeat() {
        // Pronounced heat mode vibration - longer and more intense
        vibrate(longArrayOf(0, 150, 50, 150), intArrayOf(0, 255, 0, 255))
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun vibrate(timings: LongArray, amplitudes: IntArray) {
        vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }
}
