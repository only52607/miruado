package com.ooooonly.miruado.mirai

import com.ooooonly.miruado.DICTIONARY_ROOT
import com.ooooonly.miruado.Services
import com.ooooonly.miruado.entities.BotCreateInfo
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.SimpleLogger
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class WebBotConfiguration(vertx: Vertx, private val createInfo: BotCreateInfo) : BotConfiguration(),CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    private val logPublisher: LogPublisher by lazy {
        vertx.getServiceProxy<LogPublisher>(Services.LOG)
    }
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
        fileBasedDeviceInfo(DICTIONARY_ROOT + createInfo.device)
    }
}