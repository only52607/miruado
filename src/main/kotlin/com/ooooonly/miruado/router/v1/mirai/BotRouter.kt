package com.ooooonly.miruado.router.v1.mirai

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.MiraiService
import com.ooooonly.miruado.utils.*
import io.vertx.core.Vertx
import io.vertx.ext.web.Router
import kotlinx.coroutines.CoroutineScope

class BotRouter(val vertx: Vertx, scope: CoroutineScope): Router by vertx.createRouter() {
    private val miraiService by vertx.provideService<MiraiService>(Services.BOT)
    init {
        get("/").responseSuspendEndWith(scope){
            miraiService.getAllBotsInfo()
        }
        get("/:botId").responseSuspendEndWith(scope) {
            miraiService.getBotInfo(pathParam("botId").toLong())
        }
        post("/").responseSuspendEndWith(scope,StatusCode.CREATED) {
            miraiService.createBot(bodyAsJson)
        }
        delete("/:botId").responseSuspendEndWith(scope,StatusCode.DELETED) {
            miraiService.deleteBot(pathParam("botId").toLong())
        }
        post("/:botId/captchaResult").responseSuspendEndWith(scope,StatusCode.SUCCESS) {
            miraiService.finishPicCaptcha(pathParam("botId").toLong(),bodyAsJson.getString("result"))
        }
    }
}