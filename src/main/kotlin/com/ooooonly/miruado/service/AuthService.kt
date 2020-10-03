package com.ooooonly.miruado.service

import io.vertx.core.json.JsonObject

interface AuthService {
    companion object{
        const val TOKEN_KEY: String = "Authorization"
    }
    suspend fun authCheck(token:String)
    suspend fun getPrincipal(token:String):JsonObject
    suspend fun generateToken(authData:JsonObject):String
}