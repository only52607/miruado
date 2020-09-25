package com.ooooonly.miruado

import io.vertx.core.json.JsonObject
import java.io.File
import java.io.FileWriter

const val DICTIONARY_ROOT = "/config"
val globalConfig: JsonObject by lazy {
    val defaultConfig = JsonObject()
        .put("port", 80)
        .put("auth", JsonObject().put("account", "admin").put("password", "admin"))
        .put("upload","/scripts")
        .put("device","/device.json")
    val configFile = File("$DICTIONARY_ROOT/config.json")
    if (!configFile.exists()) {
        configFile.parentFile.mkdirs()
        FileWriter(configFile).apply { write(defaultConfig.encodePrettily()) }.close()
        return@lazy defaultConfig
    }
    JsonObject(configFile.readText())
}