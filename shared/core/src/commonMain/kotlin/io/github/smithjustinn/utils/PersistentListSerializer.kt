package io.github.smithjustinn.utils

import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.toPersistentList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A custom serializer for [PersistentList] that delegates to a standard [ListSerializer].
 * This allows kotlinx.serialization to properly serialize/deserialize PersistentList types.
 */
open class PersistentListSerializer<T>(
    elementSerializer: KSerializer<T>,
) : KSerializer<PersistentList<T>> {
    private val delegateSerializer = ListSerializer(elementSerializer)

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: PersistentList<T>,
    ) {
        delegateSerializer.serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): PersistentList<T> =
        delegateSerializer.deserialize(decoder).toPersistentList()
}
