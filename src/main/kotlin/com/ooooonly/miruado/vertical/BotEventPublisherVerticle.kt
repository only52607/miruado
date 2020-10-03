package com.ooooonly.miruado.vertical

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.BotEventPublisher
import com.ooooonly.miruado.utils.eventBus
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonObject


class BotEventPublisherVerticle(channel:String):RpcCoroutineVerticle(channel), BotEventPublisher {
    companion object{
        data class Config(
            val publishChannel:String = "sockJs.bot.event"
        )
    }

    private val configProvider by provideConfig<Config>(Services.CONFIG)

    override suspend fun publishEventJson(jsonObject: JsonObject) {
        eventBus.publish(configProvider.get().publishChannel,jsonObject)
    }
}