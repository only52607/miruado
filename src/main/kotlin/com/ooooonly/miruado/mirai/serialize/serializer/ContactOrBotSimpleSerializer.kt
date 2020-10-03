package com.ooooonly.miruado.mirai.serialize.serializer

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import net.mamoe.mirai.contact.ContactOrBot

class ContactOrBotSimpleSerializer: JsonSerializer<ContactOrBot>() {
    override fun serialize(value: ContactOrBot?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeNumber(value?.id?:0)
    }
}