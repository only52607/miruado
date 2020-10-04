package com.ooooonly.miruado.vertical

import com.fasterxml.jackson.databind.ObjectMapper
import com.ooooonly.miruado.Services
import com.ooooonly.miruado.entity.BotCreateInfo
import com.ooooonly.miruado.mirai.WebBotConfiguration
import com.ooooonly.miruado.mirai.serialize.EventMapper
import com.ooooonly.miruado.mirai.serialize.MiraiObjectMapper
import com.ooooonly.miruado.service.BotEventPublisher
import com.ooooonly.miruado.service.MiraiService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.miruado.utils.ServerErrorResponseException
import com.ooooonly.miruado.utils.provideService
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot
import net.mamoe.mirai.event.events.BotEvent
import net.mamoe.mirai.event.subscribeAlways
import org.apache.logging.log4j.LogManager
import java.util.*

class MiraiVerticle(channel:String):RpcCoroutineVerticle(channel), MiraiService {
    companion object {
        data class Config(
            val loginSolverChannel: String = "sockJs.bot.loginSolver"
        )
    }

    private val configProvider by provideConfig<Config>(Services.CONFIG)
    private val scriptService by provideService<ScriptService>(Services.SCRIPT)
    private val botEventPublisher by provideService<BotEventPublisher>(Services.BOT_EVENT)
    private val captchaResults = mutableMapOf<Long, CompletableDeferred<String>>()
    private val logger = LogManager.getLogger()

    override suspend fun getAllBotsInfo(): JsonArray =
        JsonArray().apply {
            Bot.botInstances.forEach {
                val s = MiraiObjectMapper.instance.writeValueAsString(it)
                add(JsonObject(s))
            }
        }

    override suspend fun getBotInfo(botId: Long): JsonObject =
        JsonObject(MiraiObjectMapper.instance.writeValueAsString(Bot.getInstanceOrNull(botId)?:throw NotFoundResponseException()))

    override suspend fun createBot(createInfoJson: JsonObject) {
        val createInfo = ObjectMapper().readValue(createInfoJson.encode(), BotCreateInfo::class.java)
        val bot = Bot(createInfo.id, createInfo.password, WebBotConfiguration(vertx,createInfo))
        var exception: Exception? = null
        logger.info("Bot ${createInfo.id} is creating...")
        withContext(Dispatchers.IO) {
            try {
                bot.login()
                bot.subscribeAlways<BotEvent> { botEventPublisher.publishEventJson(JsonObject(EventMapper.instance.writeValueAsString(it))) }
                scriptService.addBot(bot.id)
            } catch (e: Exception) {
                e.printStackTrace()
                exception = e
            }
        }
        logger.info("Bot ${createInfo.id} created!")
        exception?.let {
            throw ServerErrorResponseException("创建失败！${it.message}")
        }
    }

    override suspend fun deleteBot(botId: Long) = Bot.getInstanceOrNull(botId)?.close()?:throw NotFoundResponseException()

    override suspend fun setBotInfo(botInfo: JsonObject) {
        TODO("Not yet implemented")
    }

    override suspend fun getFriendInfo(botId: Long, friendId: Long): JsonObject {
        TODO("Not yet implemented")
    }

    override suspend fun sendFriendMessage(botId: Long, friendId: Long, message: JsonObject) {
        TODO("Not yet implemented")
    }

    override suspend fun getGroupInfo(botId: Long, groupId: Long): JsonObject {
        TODO("Not yet implemented")
    }

    override suspend fun sendGroupMessage(botId: Long, groupId: Long, message: JsonObject) {
        TODO("Not yet implemented")
    }

    override suspend fun getMemberInfo(botId: Long, groupId: Long, memberId: Long): JsonObject {
        TODO("Not yet implemented")
    }

    override suspend fun sendMemberMessage(botId: Long, groupId: Long, memberId: Long, message: JsonObject) {
        TODO("Not yet implemented")
    }

    override suspend fun requirePicCaptcha(botId: Long, data: ByteArray): String {
        val result = CompletableDeferred<String>()
        captchaResults[botId] = result
        vertx.eventBus().publish(configProvider.get().loginSolverChannel,JsonObject().put("type", "PicCaptcha").put("data", Base64.getEncoder().encodeToString(data)))
        return result.await()
    }

    override suspend fun requireSliderCaptcha(botId: Long, url: String): String {
        val result = CompletableDeferred<String>()
        captchaResults[botId] = result
        vertx.eventBus().publish(configProvider.get().loginSolverChannel, JsonObject().put("type", "SliderCaptcha").put("url", url).encode())
        return result.await()
    }

    override suspend fun requireUnsafeDeviceLoginVerify(botId: Long, url: String): String {
        val result = CompletableDeferred<String>()
        captchaResults[botId] = result
        vertx.eventBus().publish(configProvider.get().loginSolverChannel,JsonObject().put("type", "UnsafeDeviceLoginVerify").put("url", url).encode())
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