package com.ooooonly.miruado.utils

import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Route
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

fun Vertx.createRouter(): Router = Router.router(this)
fun Route.handlerApply(fn: RoutingContext.() -> Unit): Route = handler { it.fn() }
fun Route.coroutineHandler(coroutineScope: CoroutineScope, fn: suspend (RoutingContext) -> Unit): Route =
    handler { ctx ->
        coroutineScope.launch(ctx.vertx().dispatcher()) {
            try {
                fn(ctx)
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
fun Route.coroutineHandlerApply(coroutineScope: CoroutineScope, fn: suspend RoutingContext.() -> Unit): Route =
    handler { ctx ->
        coroutineScope.launch(ctx.vertx().dispatcher()) {
            try {
                ctx.fn()
            } catch (e: Exception) {
                ctx.fail(e)
            }
        }
    }
fun Route.failureHandlerApply(fn: RoutingContext.() -> Unit): Route = failureHandler { it.fn() }
val Any.responseString: String
    get() = when (this) {
        is String -> this
        is JsonObject -> this.encode()
        is JsonArray -> this.encode()
        is List<*> -> JsonArray(this).encode()
        else -> JsonObject.mapFrom(this).encode()
    }
fun <T> Route.responseEndWith(statusCode:Int = 200,responseData:String? = null,block:RoutingContext.() -> T){
    handler {
        val result = it.block()?.responseString
        it.response().end(responseData?:result)
    }
}
fun <T> Route.responseEndWith(status:StatusCode,block:RoutingContext.() -> T) {
    return responseEndWith(status.statusCode,status.statusMessage,block)
}
fun <T> Route.responseSuspendEndWith(scope: CoroutineScope,statusCode:Int = 200,responseData:String? = null,block:suspend RoutingContext.() -> T){
    coroutineHandler(scope) {
        val result = it.block()?.responseString
        it.response().end(responseData?:result)
    }
}
fun <T> Route.responseSuspendEndWith(scope: CoroutineScope,status:StatusCode,block:suspend RoutingContext.() -> T) {
    return responseSuspendEndWith(scope,status.statusCode,status.statusMessage,block)
}