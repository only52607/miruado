package com.ooooonly.miruado.mirai

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.entity.BotCreateInfo
import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.miruado.service.LogPublisher
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import net.mamoe.mirai.utils.BotConfiguration
import net.mamoe.mirai.utils.SimpleLogger
import org.apache.logging.log4j.LogManager
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

class WebBotConfiguration(vertx: Vertx, createInfo: BotCreateInfo) : BotConfiguration(),CoroutineScope {
    override val coroutineContext: CoroutineContext = EmptyCoroutineContext
    private val logger = LogManager.getLogger()
    private val logPublisher by lazy { vertx.getServiceProxy<LogPublisher>(Services.LOG) }
    private val configService by lazy { vertx.getServiceProxy<JsonConfigProvider>(Services.CONFIG) }
    private val String.protocolValue
        get() = when(this){
            "ANDROID_PHONE" -> MiraiProtocol.ANDROID_PHONE
            "ANDROID_PAD" -> MiraiProtocol.ANDROID_PAD
            "ANDROID_WATCH" -> MiraiProtocol.ANDROID_WATCH
            else -> MiraiProtocol.ANDROID_PAD
        }
    init {
        botLoggerSupplier = {
            SimpleLogger("") { message, _ ->
                logger.debug(message)
                launch { logPublisher.publishBotLog(it.id,message?:"") }
            }
        }
        networkLoggerSupplier = {
            SimpleLogger("") { message, _ ->
                logger.debug(message)
                launch { logPublisher.publishNetLog(it.id,message?:"")}
            }
        }
        loginSolver = WebBotLoginSolver(vertx)
        protocol = createInfo.protocol.protocolValue
        launch { fileBasedDeviceInfo(configService.getAbsoluteConfigDictionary() + createInfo.device) }
    }
}