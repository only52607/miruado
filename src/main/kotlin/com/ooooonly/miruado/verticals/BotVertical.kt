package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.entities.BotCreateInfo
import com.ooooonly.miruado.entities.BotInfo
import com.ooooonly.miruado.mirai.WebBotConfiguration
import com.ooooonly.miruado.service.BotService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.miruado.utils.ServerErrorResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import java.util.*

class BotVertical(channel:String, private val loginSolverChannel: String):RpcCoroutineVerticle(channel), BotService {

    private val scriptService: ScriptService by lazy {
        vertx.getServiceProxy<ScriptService>(Services.SCRIPT)
    }

    private val captchaResults = mutableMapOf<Long, CompletableDeferred<String>>()

    override suspend fun getAllBotsInfo(): List<BotInfo> = BotInfo.fromBots().filter { it.isOnline }

    override suspend fun getBotInfo(botId: Long): BotInfo = BotInfo.fromBot(botId)

    override suspend fun createBot(createInfo: BotCreateInfo) {
        val bot = Bot(createInfo.id, createInfo.password, WebBotConfiguration(vertx,createInfo))
        var exception: Exception? = null
        withContext(Dispatchers.IO) {
            try {
                bot.login()
                scriptService.addBot(bot.id)
            } catch (e: Exception) {
                e.printStackTrace()
                exception = e
            }
        }
        exception?.let {
            throw ServerErrorResponseException("创建失败！${exception?.message}")
        }
    }

    override suspend fun deleteBot(botId: Long) = Bot.getInstanceOrNull(botId)?.close()?:throw NotFoundResponseException()

    override suspend fun setBotInfo(botInfo: BotInfo) {
        TODO("Not yet implemented")
    }

    override suspend fun requirePicCaptcha(botId: Long, data: ByteArray): String {
        val result = CompletableDeferred<String>()
        captchaResults[botId] = result
        vertx.eventBus().publish(loginSolverChannel,JsonObject().put("type", "PicCaptcha").put("data", Base64.getEncoder().encodeToString(data)))
        return result.await()
    }

    override suspend fun requireSliderCaptcha(botId: Long, url: String): String {
        val result = CompletableDeferred<String>()
        captchaResults[botId] = result
        vertx.eventBus().publish(loginSolverChannel, JsonObject().put("type", "SliderCaptcha").put("url", url).encode())
        return result.await()
    }

    override suspend fun requireUnsafeDeviceLoginVerify(botId: Long, url: String): String {
        val result = CompletableDeferred<String>()
        captchaResults[botId] = result
        vertx.eventBus().publish(loginSolverChannel,JsonObject().put("type", "UnsafeDeviceLoginVerify").put("url", url).encode())
        return result.await()
    }

    override suspend fun finishPicCaptcha(botId: Long, result: String) {
        captchaResults[botId]?.complete(result)?:throw NotFoundResponseException()
    }

    override suspend fun finishSliderCaptcha(botId: Long, result: String) {
        captchaResults[botId]?.complete(result)?:throw NotFoundResponseException()
    }

    override suspend fun finishUnsafeDeviceLoginVerify(botId: Long, result: String) {
        captchaResults[botId]?.complete(result)?:throw NotFoundResponseException()
    }

}