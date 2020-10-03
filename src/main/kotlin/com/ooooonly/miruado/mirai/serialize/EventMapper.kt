package com.ooooonly.miruado.mirai.serialize

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class EventMapper: ObjectMapper() {
    companion object{
        val instance:EventMapper by lazy { EventMapper() }
    }
    init {
        registerModule(EventSerializeModule())
        registerKotlinModule()
        setFilterProvider(EventSerializeModule.filterProvider)
    }
}