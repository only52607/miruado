package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonObject
import java.io.File

class JsonConfigVertical(channel:String):RpcCoroutineVerticle(channel), JsonConfigProvider {
    companion object{
        const val DICTIONARY_ROOT = "/config"
        const val CONFIG_PATH = "$DICTIONARY_ROOT/config.json"
    }

    private lateinit var root: JsonObject

    override suspend fun start() {
        super.start()
        root = File(CONFIG_PATH).takeIf { it.exists() }?.let{
            try {
                JsonObject(it.readText())
            }catch (e:Exception){
                JsonObject()
            }
        }?:JsonObject()
    }

    override suspend fun getConfig(key: String): JsonObject = try {
        root.getJsonObject(key)
    } catch (e:Exception) {
        JsonObject()
    }

    override suspend fun setConfig(key: String, config: JsonObject) {
        root.put(key,config)
        File(CONFIG_PATH).writeText(root.encodePrettily())
    }

    override suspend fun getAbsoluteConfigDictionary(): String  = DICTIONARY_ROOT
}