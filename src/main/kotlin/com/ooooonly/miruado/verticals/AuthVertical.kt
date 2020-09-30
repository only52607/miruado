package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.miruado.utils.UnauthorizedResponseException
import com.ooooonly.miruado.utils.mapToConfigObject
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.json.JsonObject
import io.vertx.ext.auth.JWTOptions
import io.vertx.ext.auth.jwt.JWTAuth
import io.vertx.ext.auth.jwt.JWTAuthOptions
import io.vertx.kotlin.ext.auth.pubSecKeyOptionsOf
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class AuthVertical(channel:String):RpcCoroutineVerticle(channel), AuthService {
    companion object{
        data class Config(
            val algorithm: String = "HS256",
            val publicKey: String = "oooonly",
            val secretKey: String = "ooooonlyok",
            val symmetric: Boolean = true,
            val username: String = "admin",
            val password: String = "admin"
        )
        const val JWT_STRING = "jwt"
    }

    private val configService by lazy { vertx.getServiceProxy<JsonConfigProvider>(Services.CONFIG) }
    private lateinit var verticalConfig: Config

    override suspend fun start() {
        super.start()
        verticalConfig = configService.getConfig("auth").mapToConfigObject()
        configService.setConfig("auth",JsonObject.mapFrom(verticalConfig))
    }

    private val authProvider: JWTAuth by lazy{
        JWTAuth.create(vertx, JWTAuthOptions()
            .addPubSecKey(
                pubSecKeyOptionsOf(
                    algorithm = verticalConfig.algorithm,
                    publicKey = verticalConfig.publicKey,
                    secretKey = verticalConfig.secretKey,
                    symmetric = verticalConfig.symmetric
                )
            )
        )
    }

    override suspend fun authCheck(token: String):Unit = suspendCoroutine { con ->
        authProvider.authenticate(JsonObject().put(JWT_STRING, token)) {
            if(!it.succeeded()) con.resumeWithException(UnauthorizedResponseException())
            else con.resume(Unit)
        }
    }

    override suspend fun getPrincipal(token: String): JsonObject = suspendCoroutine { con ->
        authProvider.authenticate(JsonObject().put(JWT_STRING, token)) {
            if(!it.succeeded()) con.resumeWithException(UnauthorizedResponseException())
            else con.resume(it.result().principal())
        }
    }

    override suspend fun generateToken(authData: JsonObject): String {
        if (verticalConfig.username != authData.getString("username") || verticalConfig.password != authData.getString("password")) throw UnauthorizedResponseException()
        return authProvider.generateToken(JsonObject().put("auth", true), JWTOptions())
    }
}