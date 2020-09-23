package com.ooooonly.miruado.verticals

import com.ooooonly.luaMirai.frontend.web.utils.responseOkEnd
import com.ooooonly.luaMirai.frontend.web.utils.responseUnauthorizedEnd
import com.ooooonly.luaMirai.lua.ScriptInfo
import com.ooooonly.luaMirai.lua.ScriptManager
import com.ooooonly.miruado.Config
import com.ooooonly.miruado.entities.FileInfo
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.InvalidResponseException
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.miruado.utils.UnauthorizedResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthVertical(channel:String):RpcCoroutineVerticle(channel), AuthService {
    private val authProvider: JWTAuth by lazy{
        JWTAuth.create(vertx, Config.JWT.jwtAuthOptions)
    }
    override suspend fun authCheck(token: String):Unit = suspendCoroutine { con ->
        authProvider.authenticate(JsonObject().put("jwt", token)) {
            if(!it.succeeded()) con.resumeWithException(UnauthorizedResponseException())
            else con.resume(Unit)
        }
    }

    override suspend fun getPrincipal(token: String): JsonObject = suspendCoroutine { con ->
        authProvider.authenticate(JsonObject().put("jwt", token)) {
            if(!it.succeeded()) con.resumeWithException(UnauthorizedResponseException())
            else con.resume(it.result().principal())
        }
    }

    override suspend fun generateToken(authData: JsonObject): String {
        val username = authData.getString("username")
        val password = authData.getString("password")
        val configAuth = Config.Deploy.AUTH_INFO
        val configUsername = configAuth.getString("account")
        val configPassword = configAuth.getString("password")
        if (configUsername != username || configPassword != password) throw UnauthorizedResponseException()
        return authProvider.generateToken(JsonObject().put("auth", true), JWTOptions())
    }

}