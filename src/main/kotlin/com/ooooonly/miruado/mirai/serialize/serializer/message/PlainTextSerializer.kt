package com.ooooonly.miruado.mirai.serialize.serializer.message

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.message.data.PlainText

class PlainTextSerializer: JsonSerializer<PlainText>() {
    override fun serialize(value: PlainText?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeString(value?.content?:"")
    }
}