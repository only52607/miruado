package com.ooooonly.miruado.utils

import io.vertx.core.Handler
import io.vertx.ext.web.RoutingContext
import java.io.File

class SinglePageStaticHandler(val index:String):Handler<RoutingContext> {
    companion object{
        fun create(index:String) = SinglePageStaticHandler(index)
    }
    private val content = File(index).readText()
    override fun handle(event: RoutingContext?) {
        event?.response()?.putHeader("Content-Type","text/html;charset=UTF-8")?.end(content)
    }
}