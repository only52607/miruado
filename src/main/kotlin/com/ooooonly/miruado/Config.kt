package com.ooooonly.miruado

import io.vertx.config.ConfigRetriever
import io.vertx.config.ConfigRetrieverOptions
import io.vertx.config.ConfigStoreOptions
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.kotlin.config.getConfigAwait
import java.io.File

const val DICTIONARY_ROOT = "/config"
const val CONFIG_PATH = "$DICTIONARY_ROOT/config.json"

inline fun <reified T> JsonObject.getOrSetDefault(key:String, defaultValue:T):T = when(defaultValue){
    is Int -> (getInteger(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is Number -> (getNumber(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is String -> (getString(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is Boolean -> (getBoolean(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is ByteArray -> (getBinary(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is JsonArray -> (getJsonArray(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is JsonObject -> (getJsonObject(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    else -> throw Exception("Unknown class!")
}

private var config:JsonObject? = null
private var _c_:Boolean = true
suspend fun Vertx.getGlobalConfig():JsonObject{
    config?.let { return it }
    File(CONFIG_PATH).takeUnless { it.exists() }.let {
        it?.parentFile?.mkdirs()
        it?.createNewFile()
        it?.writeText("{}")
    }
    val fileStore = ConfigStoreOptions().apply {
        type = "file"
        this.config = JsonObject().put("path",CONFIG_PATH)
    }
    val options = ConfigRetrieverOptions().addStore(fileStore)
    val configRetriever = ConfigRetriever.create(this, options)

    configRetriever.listen { change ->
        if(_c_){
            change.previousConfiguration.encodePrettily().let { File(CONFIG_PATH).writeText(it) }
        }else{
            change.newConfiguration.encodePrettily().let { File(CONFIG_PATH).writeText(it) }
        }
        _c_ = !_c_
//        change.newConfiguration = change.previousConfiguration
//        change.toJson().encodePrettily().let(::println)
    }
    return configRetriever.getConfigAwait().also { config = it }
}
