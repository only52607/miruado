package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Config
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.utils.UnauthorizedResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthVertical(channel:String):RpcCoroutineVerticle(channel), AuthService {
    private val jwtAuthOptions: JWTAuthOptions by lazy {
        JWTAuthOptions()
            .addPubSecKey(
                pubSecKeyOptionsOf(
                    algorithm = "HS256",
                    publicKey = "oooonly",
                    secretKey = "ooooonlyok",
                    symmetric = true
                )
            )
    }

    private val authProvider: JWTAuth by lazy{
        JWTAuth.create(vertx, jwtAuthOptions)
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