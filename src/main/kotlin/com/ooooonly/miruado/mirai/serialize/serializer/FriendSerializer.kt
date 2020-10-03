package com.ooooonly.miruado.mirai.serialize.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.contact.Friend
import net.mamoe.mirai.contact.nameCardOrNick

class FriendSerializer: JsonSerializer<Friend>() {
    override fun serialize(value: Friend?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeNumberField("id",value?.id?:0)
        gen?.writeStringField("nick",value?.nick)
        gen?.writeStringField("avatarUrl",value?.avatarUrl)
        gen?.writeStringField("nameCardOrNick",value?.nameCardOrNick?:"")
    }
}