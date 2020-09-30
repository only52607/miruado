package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.miruado.utils.mapToConfigObject
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject

class LogPublisherVertical(channel:String):RpcCoroutineVerticle(channel), LogPublisher {
    companion object{
        data class Config(
            val publishChannel:String = "sockJs.bot.log"
        )
    }


    private val eventBus: EventBus by lazy { vertx.eventBus() }
    private val configService by lazy { vertx.getServiceProxy<JsonConfigProvider>(Services.CONFIG) }
    private lateinit var verticalConfig: Config

    override suspend fun start() {
        super.start()
        verticalConfig = configService.getConfig("log").mapToConfigObject()
        configService.setConfig("log",JsonObject.mapFrom(verticalConfig))
    }

    override suspend fun publishLog(logString: String){
        eventBus.publish(verticalConfig.publishChannel,logString)
    }
    override suspend fun publishNetLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "bot").put("from", fromBotId).put("message", message).encode())
    }

    override suspend fun publishBotLog(fromBotId:Long,message: String) {
        publishLog(JsonObject().put("type", "net").put("from", fromBotId).put("message", message).encode())
    }

}