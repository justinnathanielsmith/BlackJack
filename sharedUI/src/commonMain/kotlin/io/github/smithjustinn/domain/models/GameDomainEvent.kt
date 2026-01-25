package io.github.smithjustinn.domain.models

/**
 * Events that can occur during the game, which the UI might want to react to.
 */
sealed class GameDomainEvent {
    data object CardFlipped : GameDomainEvent()

    data object MatchSuccess : GameDomainEvent()

    data object MatchFailure : GameDomainEvent()

    data object GameWon : GameDomainEvent()

    data object GameOver : GameDomainEvent()
}
