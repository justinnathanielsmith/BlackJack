package io.github.smithjustinn.domain.models

import io.github.smithjustinn.utils.PersistentListSerializer
import kotlinx.serialization.builtins.serializer

// Type-specific serializers for PersistentList
object CardStateListSerializer : PersistentListSerializer<CardState>(CardState.serializer())

object IntListSerializer : PersistentListSerializer<Int>(Int.serializer())
