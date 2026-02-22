package io.github.smithjustinn.ui.extensions

import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.semantics.Role
import io.github.smithjustinn.di.LocalAppGraph
import io.github.smithjustinn.services.HapticFeedbackType

@Suppress("ktlint:compose:modifier-composed-check")
fun Modifier.pokerClickable(
    onClick: () -> Unit,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
    role: Role? = null,
    hapticType: HapticFeedbackType = HapticFeedbackType.LIGHT,
): Modifier =
    composed {
        val haptics = LocalAppGraph.current.hapticsService
        val currentInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
        val currentIndication = indication ?: LocalIndication.current

        this
            .pointerHoverIcon(PointerIcon.Hand)
            .clickable(
                interactionSource = currentInteractionSource,
                indication = currentIndication,
                enabled = enabled,
                role = role,
                onClick = {
                    haptics.performHapticFeedback(hapticType)
                    onClick()
                },
            )
    }
