package com.ooooonly.miruado.verticals

import com.ooooonly.luaMirai.lua.ScriptInfo
import com.ooooonly.luaMirai.lua.ScriptManager
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.InvalidResponseException
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import java.io.File

class LuaScriptVertical(channel:String):RpcCoroutineVerticle(channel), ScriptService {

    private val fileService: FileService by lazy {
        vertx.getServiceProxy<FileService>("service.file")
    }

    private suspend fun <T> ifScriptIndexExist(scriptIndex:Int, block:suspend (Int)->T):T{
        if (scriptIndex >= ScriptManager.listScript().size || scriptIndex < 0) throw NotFoundResponseException("此脚本不存在!")
        return block(scriptIndex)
    }

    override suspend fun addScriptFromFile(fileName: String) {
        val scriptFile = File(fileService.checkFileAbsolutePath(fileName))
        if (!scriptFile.exists()) throw NotFoundResponseException("此脚本不存在!")
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
}