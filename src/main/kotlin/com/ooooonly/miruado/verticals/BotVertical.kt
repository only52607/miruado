package com.ooooonly.miruado.verticals

import com.ooooonly.luaMirai.lua.ScriptManager
import com.ooooonly.miruado.entities.BotCreateInfo
import com.ooooonly.miruado.entities.BotInfo
import com.ooooonly.miruado.mirai.WebBotConfiguration
import com.ooooonly.miruado.service.BotService
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.miruado.utils.ServerErrorResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.mamoe.mirai.Bot

class BotVertical(channel:String):RpcCoroutineVerticle(channel), BotService {
    override suspend fun getAllBotsInfo(): List<BotInfo> = BotInfo.fromBots().filter { it.isOnline }

    override suspend fun getBotInfo(botId: Long): BotInfo = BotInfo.fromBot(botId)

    override suspend fun createBot(createInfo: BotCreateInfo) {
        val bot = Bot(createInfo.id, createInfo.password, WebBotConfiguration(vertx.eventBus()))
        var exception: Exception? = null
        withContext(Dispatchers.IO) {
            try {
                bot.login()
                ScriptManager.loadBot(bot)
                true
            } catch (e: Exception) {
                e.printStackTrace()
                exception = e
                false
            }
        }.takeIf { it }.run {
            throw ServerErrorResponseException("创建失败！${exception?.message}")
        }
    }

    override suspend fun deleteBot(botId: Long) = Bot.getInstanceOrNull(botId)?.close()?:throw NotFoundResponseException("不存在此bot！")

    override suspend fun setBotInfo(botInfo: BotInfo) {
        TODO("Not yet implemented")
    }

}