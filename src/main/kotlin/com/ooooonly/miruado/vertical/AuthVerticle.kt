package com.ooooonly.miruado.vertical

import com.ooooonly.miruado.Services
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

class AuthVerticle(channel:String):RpcCoroutineVerticle(channel), AuthService {
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

    private val configProvider by provideConfig<Config>(Services.CONFIG)

    private var _authProvider: JWTAuth? = null

    private suspend fun getAuthProvider():JWTAuth{
        _authProvider?.let { return it }
        _authProvider = JWTAuth.create(vertx, JWTAuthOptions()
            .addPubSecKey(
                pubSecKeyOptionsOf(
                    algorithm = configProvider.get().algorithm,
                    publicKey = configProvider.get().publicKey,
                    secretKey = configProvider.get().secretKey,
                    symmetric = configProvider.get().symmetric
                )
            )
        )
        return _authProvider!!
    }

    override suspend fun authCheck(token: String) {
        val authProvider = getAuthProvider()
        suspendCoroutine<Unit> { con ->
            authProvider.authenticate(JsonObject().put(JWT_STRING, token)) {
                if(!it.succeeded()) con.resumeWithException(UnauthorizedResponseException())
                else con.resume(Unit)
            }
        }
    }

    override suspend fun getPrincipal(token: String): JsonObject  {
        val authProvider = getAuthProvider()
        return suspendCoroutine { con ->
            authProvider.authenticate(JsonObject().put(JWT_STRING, token)) {
                if(!it.succeeded()) con.resumeWithException(UnauthorizedResponseException())
                else con.resume(it.result().principal())
            }
        }
    }

    override suspend fun generateToken(authData: JsonObject): String {
        if (configProvider.get().username != authData.getString("username") || configProvider.get().password != authData.getString("password")) throw UnauthorizedResponseException()
        return getAuthProvider().generateToken(JsonObject().put("auth", true), JWTOptions())
    }
}