package com.ooooonly.miruado.service

import io.vertx.core.json.JsonObject

interface JsonConfigProvider {
    suspend fun getConfig(key:String):JsonObject
    suspend fun setConfig(key:String,config:JsonObject)
    suspend fun getAbsoluteConfigDictionary():String
}