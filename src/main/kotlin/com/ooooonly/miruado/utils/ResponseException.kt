package com.ooooonly.miruado.utils

import io.vertx.core.json.JsonObject

open class ResponseException(var code:Int = 500, var failMessage:String): Exception() {
    companion object{
        val instance by lazy{
            ResponseException(500,"")
        }
        fun forInstance(code:Int = 500, failMessage:String) = instance.apply {
            this.code = code
            this.failMessage = failMessage
        }
        fun fromException(e:Exception) = JsonObject(e.message).run {
            ResponseException(getInteger("code"),getString("message"))
        }
    }
    override val message: String?
        get() = "{\"code\":$code,\"message\":\"$failMessage\"}"
}

class NotFoundResponseException(failMessage:String = "没有找到该资源！"):ResponseException(404,failMessage)
class ServerErrorResponseException(failMessage:String = "服务器发生错误！"):ResponseException(500,failMessage)
class InvalidResponseException(failMessage:String = "资源无效！"):ResponseException(400,failMessage)
class UnauthorizedResponseException(failMessage:String = "身份验证失败！"):ResponseException(401,failMessage)
class responseForbiddenEndResponseException(failMessage:String = "请求被拒绝！"):ResponseException(403,failMessage)


