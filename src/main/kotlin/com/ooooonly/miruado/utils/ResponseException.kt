package com.ooooonly.miruado.utils

import io.vertx.core.Handler
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.RoutingContext
import org.apache.logging.log4j.LogManager

@Suppress("unused")
open class ResponseException(var code:Int = 500, var failMessage:String): Exception() {
    companion object{
        val failureHandler = Handler<RoutingContext> { context ->
            val logger = LogManager.getLogger()
            logger.error("Catch exception:")
            context.failure().checkResponseException()?.let {
                logger.error("Response exception:")
                if (!context.response().ended()) context.response().setStatusCode(it.code).end(it.failMessage)
                logger.error("${it.code} ${it.failMessage}")
            }?: run {
                logger.error("Unknown exception:")
                logger.error(context.failure())
                context.failure().printStackTrace()
                if (!context.response().ended()) context.response().setStatusCode(500).end(context.failure().message ?: "")
            }
        }
    }
    override val message: String?
        get() = "{\"code\":$code,\"message\":\"$failMessage\"}"
}
@Suppress("unused")
fun Throwable.checkResponseException():ResponseException? {
    if (this is ResponseException) return this
    return try{
        JsonObject(message).run {
            if(!this.containsKey("code") || !this.containsKey("message")) return@run null
            ResponseException(getInteger("code"),getString("message"))
        }
    }catch (e:Exception){
        null
    }
}





@Suppress("unused") class InvalidResponseException(failMessage:String = "资源无效！"):ResponseException(400,failMessage)
@Suppress("unused") class UnauthorizedResponseException(failMessage:String = "身份验证失败！"):ResponseException(401,failMessage)
@Suppress("unused") class ForbiddenResponseException(failMessage:String = "请求被拒绝！"):ResponseException(403,failMessage)
@Suppress("unused") class NotFoundResponseException(failMessage:String = "没有找到该资源！"):ResponseException(404,failMessage)
@Suppress("unused") class ServerErrorResponseException(failMessage:String = "服务器发生错误！"):ResponseException(500,failMessage)