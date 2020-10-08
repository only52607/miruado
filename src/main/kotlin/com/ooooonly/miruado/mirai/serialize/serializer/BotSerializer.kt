package com.ooooonly.miruado.mirai.serialize.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.Bot

class BotSerializer: JsonSerializer<Bot>() {
    override fun serialize(value: Bot?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeNumberField("id",value?.id?:0)
        try { gen?.writeStringField("nick",value?.nick) } catch (e:Exception) {gen?.writeStringField("nick","")}
        try { gen?.writeStringField("avatarUrl",value?.selfQQ?.avatarUrl) } catch (e:Exception) {gen?.writeStringField("avatarUrl","")}
        gen?.writeBooleanField("isOnline",value?.isOnline?:false)
        gen?.writeEndObject()
    }
}