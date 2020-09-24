package com.ooooonly.miruado.mirai

import com.ooooonly.miruado.Config
import com.ooooonly.miruado.service.BotService
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonObject
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver
import java.util.*

class WebBotLoginSolver(val vertx: Vertx) : LoginSolver() {

    private val botService: BotService by lazy {
        vertx.getServiceProxy<BotService>("service.bot")
    }

    override suspend fun onSolvePicCaptcha(bot: Bot, data: ByteArray): String? {
        return botService.requirePicCaptcha(bot.id,data)
    }

    override suspend fun onSolveSliderCaptcha(bot: Bot, url: String): String? {
        return botService.requireSliderCaptcha(bot.id,url)
    }

    override suspend fun onSolveUnsafeDeviceLoginVerify(bot: Bot, url: String): String? {
        return botService.requireUnsafeDeviceLoginVerify(bot.id,url)
    }
}