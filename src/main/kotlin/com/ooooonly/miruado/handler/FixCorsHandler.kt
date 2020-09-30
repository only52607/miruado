package com.ooooonly.miruado.handler

import io.vertx.core.Handler
import io.vertx.core.http.HttpMethod
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.CorsHandler

class FixCorsHandler(private val tokenKey:String): Handler<RoutingContext> {
    private val corsHandler by lazy {
        CorsHandler.create("*")
            .allowedMethod(HttpMethod.GET)
            .allowedMethod(HttpMethod.POST)
            .allowedMethod(HttpMethod.PUT)
            .allowedMethod(HttpMethod.DELETE)
    }
    override fun handle(event: RoutingContext?) {
        event?.response()?.putHeader(
            "Access-Control-Allow-Headers",
            "Origin, No-Cache, X-Requested-With, If-Modified-Since, Pragma, Last-Modified, Cache-Control, Expires, Content-Type, X-E4M-With, $tokenKey"
        )?.putHeader("Access-Control-Expose-Headers", tokenKey)
        corsHandler.handle(event)
    }
}