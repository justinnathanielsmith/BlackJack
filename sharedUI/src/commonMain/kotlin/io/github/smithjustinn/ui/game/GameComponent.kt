package io.github.smithjustinn.ui.game

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface GameComponent {
    val state: StateFlow<GameUIState>
    val events: Flow<GameUiEvent>

    fun onFlipCard(cardId: Int)

    fun onRestart()

    fun onBack()

    fun onToggleAudio()

    fun onWalkthroughAction(isComplete: Boolean)
}
