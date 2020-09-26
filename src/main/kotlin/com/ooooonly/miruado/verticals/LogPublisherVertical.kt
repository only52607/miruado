package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.getGlobalConfig
import com.ooooonly.miruado.getOrSetDefault
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject

class LogPublisherVertical(channel:String):RpcCoroutineVerticle(channel), LogPublisher {
    private val eventBus: EventBus by lazy {
        vertx.eventBus()
    }

    private var publishChannel = "eventBus.bot.log"

    override suspend fun start() {
        super.start()
        val rootConfig = vertx.getGlobalConfig()
        val logConfig = rootConfig.getOrSetDefault("logPublish", JsonObject())
        publishChannel = logConfig.getOrSetDefault("publishChannel",publishChannel)
        rootConfig.put("logPublish",logConfig)
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