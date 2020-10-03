package com.ooooonly.miruado.router.v1

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.router.v1.mirai.BotRouter
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.utils.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import kotlinx.coroutines.CoroutineScope

class V1ApiRouter(val vertx:Vertx, scope: CoroutineScope): Router by vertx.createRouter() {
    private val authService by vertx.provideService<AuthService>(Services.AUTH)
    init {
        route().handlerPreApply { response.putHeader("Content-Type","application/json") }
        mountSubRouter("/auth",
            AuthRouter(vertx, scope)
        )
        route().coroutineHandlerPreApply(scope){
            authService.authCheck(request.getHeader(AuthService.TOKEN_KEY))
        }
        mountSubRouter("/bots", BotRouter(vertx,scope))
        mountSubRouter("/scripts",LuaScriptRouter(vertx, scope))
        mountSubRouter("/files",FileRouter(vertx, scope))
    }
}