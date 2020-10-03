package com.ooooonly.miruado.router.v1.mirai

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.MiraiService
import com.ooooonly.miruado.utils.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import kotlinx.coroutines.CoroutineScope

class GroupRouter(val vertx: Vertx, scope: CoroutineScope): Router by vertx.createRouter() {
    private val miraiService by vertx.provideService<MiraiService>(Services.BOT)
    init {

    }
}