package io.github.smithjustinn.utils

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A custom serializer for [ImmutableList] that delegates to a standard [ListSerializer].
 * This allows kotlinx.serialization to properly serialize/deserialize ImmutableList types.
 */
open class ImmutableListSerializer<T>(
    elementSerializer: KSerializer<T>,
) : KSerializer<ImmutableList<T>> {
    private val delegateSerializer = ListSerializer(elementSerializer)

    override val descriptor: SerialDescriptor = delegateSerializer.descriptor

    override fun serialize(
        encoder: Encoder,
        value: ImmutableList<T>,
    ) {
        delegateSerializer.serialize(encoder, value.toList())
    }

    override fun deserialize(decoder: Decoder): ImmutableList<T> =
        delegateSerializer.deserialize(decoder).toImmutableList()
}
