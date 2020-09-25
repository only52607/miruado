package com.ooooonly.miruado.utils

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext

open class ResponseException(var code:Int = 500, var failMessage:String): Exception() {
    companion object{
        val failureHandler = Handler<RoutingContext> {context ->
            println("Catch exception:")
            context.failure().checkResponseException()?.let {
                println("Response exception:")
                if (!context.response().ended()) context.response().setStatusCode(it.code).end(it.failMessage)
                println("${it.code} ${it.failMessage}")
            }?: run {
                println("Unknown exception:")
                context.failure().printStackTrace()
                if (!context.response().ended()) context.response().setStatusCode(500).end(context.failure().message ?: "")
            }
        }
    }
    override val message: String?
        get() = "{\"code\":$code,\"message\":\"$failMessage\"}"
}

fun Throwable.checkResponseException():ResponseException? = try{
    JsonObject(message).run {
        if(!this.containsKey("code") || !this.containsKey("message")) return@run null
        ResponseException(getInteger("code"),getString("message"))
    }

}catch (e:Exception){
    null
}

class InvalidResponseException(failMessage:String = "资源无效！"):ResponseException(400,failMessage)
class UnauthorizedResponseException(failMessage:String = "身份验证失败！"):ResponseException(401,failMessage)
class ForbiddenResponseException(failMessage:String = "请求被拒绝！"):ResponseException(403,failMessage)
class NotFoundResponseException(failMessage:String = "没有找到该资源！"):ResponseException(404,failMessage)
class ServerErrorResponseException(failMessage:String = "服务器发生错误！"):ResponseException(500,failMessage)