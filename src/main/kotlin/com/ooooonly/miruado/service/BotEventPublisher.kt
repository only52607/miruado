package com.ooooonly.miruado.service

import io.vertx.core.json.JsonObject

interface BotEventPublisher {
    suspend fun publishEventJson(jsonObject: JsonObject)
}