package io.github.smithjustinn.services

import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
import platform.UIKit.UINotificationFeedbackGenerator
import platform.UIKit.UINotificationFeedbackType

class IosHapticsServiceImpl : HapticsService {
    // Cache generators for better performance
    private val heavyImpactGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
    private val lightImpactGenerator = UIImpactFeedbackGenerator(UIImpactFeedbackStyle.UIImpactFeedbackStyleLight)
    private val notificationGenerator = UINotificationFeedbackGenerator()

    init {
        // Prepare generators to reduce latency on first use
        heavyImpactGenerator.prepare()
        lightImpactGenerator.prepare()
        notificationGenerator.prepare()
    }

    override fun vibrateMatch() {
        heavyImpactGenerator.impactOccurred()
        heavyImpactGenerator.prepare()
    }

    override fun vibrateMismatch() {
        heavyImpactGenerator.impactOccurred()
        heavyImpactGenerator.prepare()
    }

    override fun vibrateTick() {
        lightImpactGenerator.impactOccurred()
        lightImpactGenerator.prepare()
    }

    override fun vibrateWarning() {
        notificationGenerator.notificationOccurred(UINotificationFeedbackType.UINotificationFeedbackTypeWarning)
        notificationGenerator.prepare()
    }

    override fun vibrateHeat() {
        // Pronounced heat mode vibration using heavy impact
        heavyImpactGenerator.impactOccurred()
        heavyImpactGenerator.prepare()
    }
}
