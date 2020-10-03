package com.ooooonly.miruado.mirai.serialize.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.contact.Member

class MemberSerializer: JsonSerializer<Member>() {
    override fun serialize(value: Member?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeNumberField("id",value?.id?:0)
        gen?.writeStringField("nick",value?.nick)
        gen?.writeStringField("avatarUrl",value?.avatarUrl)
        gen?.writeStringField("nameCard",value?.nameCard)
        gen?.writeStringField("specialTitle",value?.specialTitle)
        gen?.writeNumberField("muteTimeRemaining",value?.muteTimeRemaining?:0)
        gen?.writeNumberField("permission",value?.permission?.level?:0)
        gen?.writeNumber(value?.id?:0)
    }
}