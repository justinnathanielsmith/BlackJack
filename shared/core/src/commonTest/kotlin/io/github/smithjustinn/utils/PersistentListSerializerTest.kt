package io.github.smithjustinn.utils

import kotlinx.collections.immutable.persistentListOf
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class PersistentListSerializerTest {
    private val json = Json
    private val serializer = PersistentListSerializer(String.serializer())

    @Test
    fun testSerialization() {
        val list = persistentListOf("a", "b", "c")
        val string = json.encodeToString(serializer, list)
        val decoded = json.decodeFromString(serializer, string)
        assertEquals(list, decoded)
    }
}
