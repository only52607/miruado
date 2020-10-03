package com.ooooonly.miruado.mirai.serialize

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class MiraiObjectMapper: ObjectMapper() {
    companion object{
        val instance:MiraiObjectMapper by lazy { MiraiObjectMapper() }
    }
    init {
        registerModule(MiraiObjectSerializeModule())
        registerKotlinModule()
    }
}