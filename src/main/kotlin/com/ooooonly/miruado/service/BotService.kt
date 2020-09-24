package com.ooooonly.miruado.service

import com.ooooonly.miruado.entities.BotCreateInfo
import com.ooooonly.miruado.entities.BotInfo

interface BotService {
    suspend fun getAllBotsInfo():List<BotInfo>
    suspend fun getBotInfo(botId:Long):BotInfo
    suspend fun createBot(createInfo: BotCreateInfo)
    suspend fun deleteBot(botId:Long)
    suspend fun setBotInfo(botInfo:BotInfo)
    suspend fun requirePicCaptcha(botId: Long,data: ByteArray):String
    suspend fun requireSliderCaptcha(botId: Long,url: String):String
    suspend fun requireUnsafeDeviceLoginVerify(botId: Long,url: String):String
    suspend fun finishPicCaptcha(botId: Long,result: String)
    suspend fun finishSliderCaptcha(botId: Long,result: String)
    suspend fun finishUnsafeDeviceLoginVerify(botId: Long,result: String)
}