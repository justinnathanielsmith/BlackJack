package io.github.smithjustinn.domain.models

import io.github.smithjustinn.utils.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer
import org.jetbrains.compose.resources.StringResource

// Type-specific serializers for ImmutableList
internal object CardStateListSerializer : ImmutableListSerializer<CardState>(CardState.serializer())
internal object IntListSerializer : ImmutableListSerializer<Int>(Int.serializer())

/**
 * Defines the available game modes.
 */
@Serializable
enum class GameMode {
    STANDARD,
    TIME_ATTACK,
    DAILY_CHALLENGE,
}

/**
 * Configuration for game scoring and bonuses.
 */
@Serializable
data class ScoringConfig(
    val baseMatchPoints: Int = 20,
    val comboBonusPoints: Int = 50,
    val timeBonusPerPair: Int = 50,
    val timePenaltyPerSecond: Int = 1,
    val moveBonusMultiplier: Int = 10000, // Base for move efficiency
)

/**
 * Breakdown of the final score.
 */
@Serializable
data class ScoreBreakdown(
    val matchPoints: Int = 0,
    val timeBonus: Int = 0,
    val moveBonus: Int = 0,
    val totalScore: Int = 0,
)

/**
 * Represents a match comment with its resource and optional arguments.
 */
data class MatchComment(val res: StringResource, val args: ImmutableList<Any> = persistentListOf())

/**
 * Represents the core state of the memory game.
 */
@Serializable
data class MemoryGameState(
    @Serializable(with = CardStateListSerializer::class)
    val cards: ImmutableList<CardState> = persistentListOf(),
    val pairCount: Int = 8,
    val isGameWon: Boolean = false,
    val isGameOver: Boolean = false,
    val moves: Int = 0,
    val score: Int = 0,
    val comboMultiplier: Int = 1,
    @Transient val matchComment: MatchComment? = null,
    val config: ScoringConfig = ScoringConfig(),
    val scoreBreakdown: ScoreBreakdown = ScoreBreakdown(),
    val mode: GameMode = GameMode.STANDARD,
    @Serializable(with = IntListSerializer::class)
    val lastMatchedIds: ImmutableList<Int> = persistentListOf(),
)

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
