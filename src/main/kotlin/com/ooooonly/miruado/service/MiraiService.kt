package com.ooooonly.miruado.service

import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject

interface MiraiService {
    suspend fun getAllBotsInfo():JsonArray
    suspend fun getBotInfo(botId:Long):JsonObject
    suspend fun createBot(createInfo: JsonObject)
    suspend fun deleteBot(botId:Long)
    suspend fun setBotInfo(botInfo:JsonObject)

    suspend fun getFriendInfo(botId: Long,friendId:Long):JsonObject
    suspend fun sendFriendMessage(botId: Long,friendId:Long,message:JsonObject)

    suspend fun getGroupInfo(botId:Long,groupId:Long):JsonObject
    suspend fun sendGroupMessage(botId: Long,groupId:Long,message:JsonObject)

    suspend fun getMemberInfo(botId: Long,groupId: Long,memberId:Long):JsonObject
    suspend fun sendMemberMessage(botId: Long,groupId: Long,memberId:Long,message:JsonObject)

    suspend fun requirePicCaptcha(botId: Long,data: ByteArray):String
    suspend fun requireSliderCaptcha(botId: Long,url: String):String
    suspend fun requireUnsafeDeviceLoginVerify(botId: Long,url: String):String
    suspend fun finishPicCaptcha(botId: Long,result: String)
    suspend fun finishSliderCaptcha(botId: Long,result: String)
    suspend fun finishUnsafeDeviceLoginVerify(botId: Long,result: String)
}