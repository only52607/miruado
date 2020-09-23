package com.ooooonly.miruado.service

import com.ooooonly.luaMirai.lua.ScriptInfo

interface ScriptService {
    suspend fun addScriptFromFile(fileName:String)
    suspend fun reloadScript(scriptIndex:Int)
    suspend fun getAllScriptsInfo():List<ScriptInfo>
    suspend fun getScriptInfo(scriptIndex:Int):ScriptInfo
    suspend fun removeScript(scriptIndex:Int)
}