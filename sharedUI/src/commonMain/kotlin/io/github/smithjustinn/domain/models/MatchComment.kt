package io.github.smithjustinn.domain.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.jetbrains.compose.resources.StringResource

/**
 * Represents a match comment with its resource and optional arguments.
 */
data class MatchComment(
    val res: StringResource,
    val args: ImmutableList<Any> = persistentListOf(),
)
