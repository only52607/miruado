package com.ooooonly.miruado.mirai.serialize

import com.fasterxml.jackson.annotation.JsonFilter
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.FilterProvider
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider
import com.ooooonly.miruado.mirai.serialize.serializer.ContactOrBotSimpleSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.message.ImageSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.message.MessageChainSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.message.MessageSourceSerializer
import com.ooooonly.miruado.mirai.serialize.serializer.message.PlainTextSerializer
import net.mamoe.mirai.contact.ContactOrBot
import net.mamoe.mirai.event.AbstractEvent
import net.mamoe.mirai.message.data.Image
import net.mamoe.mirai.message.data.MessageChain
import net.mamoe.mirai.message.data.MessageSource
import net.mamoe.mirai.message.data.PlainText

@JsonFilter("MiraiEventJsonFilter") interface MiraiEventJsonFilter
class EventSerializeModule:SimpleModule() {
    companion object{
        val filterProvider: FilterProvider by lazy{
            SimpleFilterProvider()
                .addFilter("MiraiEventJsonFilter",
                    SimpleBeanPropertyFilter.serializeAllExcept(
                        "broadCastLock",
                        "_intercepted",
                        "shouldBroadcast",
                        "intercepted",
                        "cancelled"
                    )
                )
        }
    }
    init {
        addSerializer(ContactOrBot::class.java, ContactOrBotSimpleSerializer())
        addSerializer(MessageSource::class.java,
            MessageSourceSerializer()
        )
        addSerializer(MessageChain::class.java,
            MessageChainSerializer()
        )
        addSerializer(Image::class.java,
            ImageSerializer()
        )
        addSerializer(
            PlainText::class.java,
            PlainTextSerializer()
        )
        setMixInAnnotation(AbstractEvent::class.java,MiraiEventJsonFilter::class.java)
    }
}