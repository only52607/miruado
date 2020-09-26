package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.getGlobalConfig
import com.ooooonly.miruado.getOrSetDefault
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

    companion object{
        const val JWT_STRING = "jwt"
    }

    private var algorithm: String = "HS256"
    private var publicKey: String = "oooonly"
    private var secretKey: String = "ooooonlyok"
    private var symmetric: Boolean = true
    private var username:String = "admin"
    private var password:String = "admin"

    override suspend fun start() {
        super.start()
        val rootConfig = vertx.getGlobalConfig()
        val authConfig = rootConfig.getOrSetDefault("auth",JsonObject())
        username = authConfig.getOrSetDefault("username",username)
        password = authConfig.getOrSetDefault("password",password)

        val secKeyConfig = authConfig.getOrSetDefault("secKey",JsonObject())
        algorithm = secKeyConfig.getOrSetDefault("algorithm",algorithm)
        publicKey = secKeyConfig.getOrSetDefault("publicKey",publicKey)
        secretKey = secKeyConfig.getOrSetDefault("secretKey",secretKey)
        symmetric = secKeyConfig.getOrSetDefault("symmetric",symmetric)

        authConfig.put("secKey",secKeyConfig)
        rootConfig.put("auth",authConfig)
    }

    private val jwtAuthOptions: JWTAuthOptions by lazy {
        JWTAuthOptions()
            .addPubSecKey(
                pubSecKeyOptionsOf(
                    algorithm = algorithm,
                    publicKey = publicKey,
                    secretKey = secretKey,
                    symmetric = symmetric
                )
            )
    }

    private val authProvider: JWTAuth by lazy{
        JWTAuth.create(vertx, jwtAuthOptions)
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
        if (this.username != authData.getString("username") || this.password != authData.getString("password")) throw UnauthorizedResponseException()
        return authProvider.generateToken(JsonObject().put("auth", true), JWTOptions())
    }
}