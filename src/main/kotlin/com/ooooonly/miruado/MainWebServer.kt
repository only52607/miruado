package com.ooooonly.miruado

import com.ooooonly.miruado.verticals.ControllerVertical
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait

class MainWebServer {
    val vertx by lazy{
        Vertx.vertx()
    }
    suspend fun start(){
        vertx.deployVerticleAwait(ControllerVertical())
    }
    fun stop(){
        vertx.close()
    }
}