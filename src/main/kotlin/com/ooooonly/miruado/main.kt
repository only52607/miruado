package com.ooooonly.miruado

import com.ooooonly.miruado.verticals.WebControllerVertical
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait

private val vertx: Vertx by lazy{
    Vertx.vertx()
}

suspend fun main(){
    println("Service is Being deployed...")
    vertx.deployVerticleAwait(WebControllerVertical())
    println("Service was successfully deployed!")
}