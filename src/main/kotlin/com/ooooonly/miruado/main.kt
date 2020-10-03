package com.ooooonly.miruado

import com.ooooonly.miruado.vertical.WebControllerVerticle
import io.vertx.core.Vertx
import io.vertx.kotlin.core.deployVerticleAwait
import org.apache.logging.log4j.LogManager

private val vertx: Vertx by lazy{
    Vertx.vertx()
}

suspend fun main(){
    val logger = LogManager.getLogger()
    logger.info("Service is Being deployed...")
    vertx.deployVerticleAwait(WebControllerVerticle())
    logger.info("Service was successfully deployed!")
}