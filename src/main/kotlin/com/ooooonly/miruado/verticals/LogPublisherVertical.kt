package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject

class LogPublisherVertical(channel:String, private val publishChannel:String):RpcCoroutineVerticle(channel), LogPublisher {
    private val eventBus: EventBus by lazy {
        vertx.eventBus()
    }
    override suspend fun publishLog(logString: String){
        eventBus.publish(publishChannel,logString)
    }
    override suspend fun publishNetLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "bot").put("from", fromBotId).put("message", message).encode())
    }

    override suspend fun publishBotLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "net").put("from", fromBotId).put("message", message).encode())
    }

}