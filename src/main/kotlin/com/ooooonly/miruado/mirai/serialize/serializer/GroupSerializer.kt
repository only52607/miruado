package com.ooooonly.miruado.mirai.serialize.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.contact.Group
import net.mamoe.mirai.contact.isBotMuted

class GroupSerializer: JsonSerializer<Group>() {
    override fun serialize(value: Group?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeNumberField("id",value?.id?:0)
        gen?.writeStringField("name",value?.name)
        gen?.writeStringField("avatarUrl",value?.avatarUrl)
        gen?.writeBooleanField("isBotMuted",value?.isBotMuted?:false)
        gen?.writeObjectField("owner",value?.owner)
        gen?.writeObjectField("settings",value?.settings)
    }
}