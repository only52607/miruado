package com.ooooonly.miruado.router.v1

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.utils.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import kotlinx.coroutines.CoroutineScope

class AuthRouter(val vertx:Vertx, scope: CoroutineScope): Router by vertx.createRouter() {
    private val authService by vertx.provideService<AuthService>(Services.AUTH)
    init {
        post().responseSuspendEndWith(scope, StatusCode.SUCCESS) {
            response.putHeader(AuthService.TOKEN_KEY,authService.generateToken(bodyAsJson))
        }
        get().responseSuspendEndWith(scope) {
            authService.getPrincipal(request.getHeader(AuthService.TOKEN_KEY))
        }
    }
}