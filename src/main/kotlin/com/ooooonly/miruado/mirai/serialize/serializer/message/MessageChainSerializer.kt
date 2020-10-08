package com.ooooonly.miruado.mirai.serialize.serializer.message

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain

class MessageChainSerializer: JsonSerializer<MessageChain>() {
    override fun serialize(value: MessageChain?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartArray()
        value?.forEach {
            gen?.writeStartObject()
            when(it){
                is Image -> gen?.writeStringField("type","Image")
                else -> gen?.writeStringField("type",it::class.simpleName)
            }
            gen?.writeObjectField("content",it)
            gen?.writeEndObject()
        }
        gen?.writeEndArray()
    }
}