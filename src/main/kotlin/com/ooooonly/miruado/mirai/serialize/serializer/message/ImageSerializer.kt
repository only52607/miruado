package com.ooooonly.miruado.mirai.serialize.serializer.message

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import kotlinx.coroutines.runBlocking
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.queryUrl

class ImageSerializer: JsonSerializer<Image>() {
    override fun serialize(value: Image?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeStartObject()
        gen?.writeStringField("imageId",value?.imageId?:"")
        runBlocking {
            gen?.writeStringField("url",value?.queryUrl()?:"")
        }
        gen?.writeEndObject()
    }
}