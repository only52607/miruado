package com.ooooonly.miruado.mirai.serialize

import net.mamoe.mirai.event.Event


data class EventWrapper(
    var type:String = "",
    val content: Event? = null
){
    init {
        type = content?.let { it::class.simpleName }?:""
    }
}