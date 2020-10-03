package com.ooooonly.miruado.mirai

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.MiraiService
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import net.mamoe.mirai.Bot
import net.mamoe.mirai.utils.LoginSolver

class WebBotLoginSolver(val vertx: Vertx) : LoginSolver() {

    private val botService by lazy { vertx.getServiceProxy<MiraiService>(Services.BOT) }

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