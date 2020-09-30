package com.ooooonly.miruado.utils

import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
@Suppress("unused")
inline fun <reified T> RoutingContext.getBodyAsObject(): T =
    ObjectMapper().readValue(bodyAsString, T::class.java)
@Suppress("unused")
val RoutingContext.response: HttpServerResponse
    get() = this.response()
@Suppress("unused")
val RoutingContext.request: HttpServerRequest
    get() = this.request()