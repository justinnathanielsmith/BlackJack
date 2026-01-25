package io.github.smithjustinn.domain.models

import io.github.smithjustinn.utils.ImmutableListSerializer
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.builtins.serializer

// Type-specific serializers for ImmutableList
internal object CardStateListSerializer : ImmutableListSerializer<CardState>(CardState.serializer())

internal object IntListSerializer : ImmutableListSerializer<Int>(Int.serializer())

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
    val seed: Long? = null,
)
