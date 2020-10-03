package com.ooooonly.miruado.vertical

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.miruado.utils.eventBus
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonObject

class LogPublisherVerticle(channel:String):RpcCoroutineVerticle(channel), LogPublisher {
    companion object{
        data class Config(
            val publishChannel:String = "sockJs.bot.log"
        )
    }

    private val configProvider by provideConfig<Config>(Services.CONFIG)

    override suspend fun publishLog(logString: String){
        eventBus.publish(configProvider.get().publishChannel,logString)
    }
    override suspend fun publishNetLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "bot").put("from", fromBotId).put("message", message).encode())
    }
    override suspend fun publishBotLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "net").put("from", fromBotId).put("message", message).encode())
    }
}