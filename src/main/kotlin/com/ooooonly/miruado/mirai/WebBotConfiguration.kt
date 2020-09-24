package com.ooooonly.miruado.mirai

import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.SimpleLogger

class WebBotConfiguration(vertx: Vertx) : BotConfiguration() {
    private val logPublisher: LogPublisher by lazy {
        vertx.getServiceProxy<LogPublisher>("service.log")
    }
    init {
        botLoggerSupplier = {
            SimpleLogger("") { message, e ->
                println(message)
                logPublisher.publishBotLog(it.id,message?:"")
            }
        }
        networkLoggerSupplier = {
            SimpleLogger("") { message, e ->
                println(message)
                logPublisher.publishNetLog(it.id,message?:"")
            }
        }
        loginSolver = WebBotLoginSolver(vertx)
    }
}