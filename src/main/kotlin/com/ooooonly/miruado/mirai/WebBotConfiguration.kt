package com.ooooonly.miruado.mirai

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.entities.BotCreateInfo
import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.SimpleLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class WebBotConfiguration(vertx: Vertx, createInfo: BotCreateInfo) : BotConfiguration(),CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    private val logPublisher by lazy { vertx.getServiceProxy<LogPublisher>(Services.LOG) }
    private val configService by lazy { vertx.getServiceProxy<JsonConfigProvider>(Services.CONFIG) }
    init {
        botLoggerSupplier = {
            SimpleLogger("") { message, _ ->
                println(message)
                launch { logPublisher.publishBotLog(it.id,message?:"") }
            }
        }
        networkLoggerSupplier = {
            SimpleLogger("") { message, _ ->
                println(message)
                launch { logPublisher.publishNetLog(it.id,message?:"")}
            }
        }
        loginSolver = WebBotLoginSolver(vertx)
        launch { fileBasedDeviceInfo(configService.getAbsoluteConfigDictionary() + createInfo.device) }
    }
}