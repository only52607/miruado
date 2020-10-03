package com.ooooonly.miruado.mirai.serialize.serializer.message

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.message.data.MessageSource

class MessageSourceSerializer: JsonSerializer<MessageSource>() {
    override fun serialize(value: MessageSource?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeNumberField("id",value?.id?:0)
        gen?.writeNumberField("fromId",value?.fromId?:0)
        gen?.writeNumberField("targetId",value?.targetId?:0)
        gen?.writeNumberField("time",value?.time?:0)
        gen?.writeNumberField("internalId",value?.internalId?:0)
        gen?.writeEndObject()
    }
}