package io.github.smithjustinn.utils

/**
 * Interface for screens that need to handle back press events.
 */
interface BackPressScreen {
    /**
     * Handles the back press event.
     * @return true if the back press was handled and the navigator should pop, false otherwise.
     */
    fun handleBack(): Boolean
}
