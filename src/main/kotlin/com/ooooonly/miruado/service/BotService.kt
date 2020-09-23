package com.ooooonly.miruado.service

import com.ooooonly.miruado.entities.BotCreateInfo
import com.ooooonly.miruado.entities.BotInfo

interface BotService {
    suspend fun getAllBotsInfo():List<BotInfo>
    suspend fun getBotInfo(botId:Long):BotInfo
    suspend fun createBot(createInfo: BotCreateInfo)
    suspend fun deleteBot(botId:Long)
    suspend fun setBotInfo(botInfo:BotInfo)
}