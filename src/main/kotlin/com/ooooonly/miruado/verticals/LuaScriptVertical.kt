package com.ooooonly.miruado.verticals

import com.ooooonly.luaMirai.lua.ScriptInfo
import com.ooooonly.luaMirai.lua.ScriptManager
import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.InvalidResponseException
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import java.io.File

class LuaScriptVertical(channel:String):RpcCoroutineVerticle(channel), ScriptService {

    private val fileService by lazy {
        vertx.getServiceProxy<FileService>(Services.FILE)
    }

    private suspend fun <T> ifScriptIndexExist(scriptIndex:Int, block:suspend (Int)->T):T{
        if (scriptIndex >= ScriptManager.listScript().size || scriptIndex < 0) throw NotFoundResponseException()
        return block(scriptIndex)
    }

    override suspend fun addScriptFromFile(fileName: String) {
        val scriptFile = File(fileService.checkFileAbsolutePath(fileName))
        if (!scriptFile.exists()) throw NotFoundResponseException()
        try {
            ScriptManager.addScript(scriptFile)
        } catch (e: Exception) {
            throw InvalidResponseException(e.message?:"")
        }
    }

    override suspend fun reloadScript(scriptIndex: Int) = ifScriptIndexExist(scriptIndex){
        ScriptManager.reloadScript(it)
    }

    override suspend fun getAllScriptsInfo(): List<ScriptInfo> = ScriptManager.listScript().map { it.info }

    override suspend fun getScriptInfo(scriptIndex: Int): ScriptInfo = ifScriptIndexExist(scriptIndex) {
        ScriptManager.listScript()[it].info
    }

    override suspend fun removeScript(scriptIndex: Int) = ifScriptIndexExist(scriptIndex) {
        ScriptManager.removeScript(it)
    }

    override suspend fun addBot(botId: Long) {
        try {
            ScriptManager.loadBotFromId(botId)
        }catch (e:Exception){
            throw NotFoundResponseException()
        }
    }
}