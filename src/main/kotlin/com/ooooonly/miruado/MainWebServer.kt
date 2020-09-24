package com.ooooonly.miruado

import com.ooooonly.miruado.verticals.*
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait

class MainWebServer {
    val vertx by lazy{
        Vertx.vertx()
    }
    suspend fun start(){
        vertx.deployVerticleAwait(WebControllerVertical(Config.Deploy.PORT))
    }
    fun stop(){
        vertx.close()
    }
}