package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Config
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonObject

class LogPublisherVertical(channel:String, private val publishChannel:String):RpcCoroutineVerticle(channel), LogPublisher {
    override fun publishLog(logString: String){
        vertx.eventBus().publish(publishChannel,logString)
    }

    override fun publishNetLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "bot").put("from", fromBotId).put("message", message).encode())
    }

    override fun publishBotLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "net").put("from", fromBotId).put("message", message).encode())
    }

}