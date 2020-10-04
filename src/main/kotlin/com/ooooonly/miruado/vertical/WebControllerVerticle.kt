package com.ooooonly.miruado.vertical

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.handler.FixCorsHandler
import com.ooooonly.miruado.router.v1.V1ApiRouter
import com.ooooonly.miruado.service.*
import com.ooooonly.miruado.utils.*
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.web.handler.sockjs.permittedOptionsOf
import io.vertx.kotlin.ext.web.handler.sockjs.sockJSBridgeOptionsOf

class WebControllerVerticle:CoroutineVerticle() {
    companion object{
        data class Config(
            val port: Int = 80,
            val handleStatic: Boolean = true,
            val useCustomStatic: Boolean = false,
            val customStaticDictionary: String = "",
            val eventBusPublishAddressRegex: String = "sockJs\\..+"
        )
    }

    private val fileService by provideService<FileService>(Services.FILE)
    private val configProvider by provideConfig<Config>(Services.CONFIG)

    override suspend fun start() {
        deployVerticles()
        val mainRouter = vertx.createRouter()
        mainRouter.route()
            .handler(FixCorsHandler(AuthService.TOKEN_KEY))
            .handler(BodyHandler.create().setUploadsDirectory(fileService.getUploadPath()).setDeleteUploadedFilesOnEnd(true))
            .handler(ResponseContentTypeHandler.create())
            .failureHandler(ResponseException.failureHandler)
        mainRouter.mountSubRouter("/eb",SockJSHandler.create(vertx).bridge(
            sockJSBridgeOptionsOf(outboundPermitted = listOf(permittedOptionsOf(addressRegex = configProvider.get().eventBusPublishAddressRegex)) )
        ))
        mainRouter.mountSubRouter("/api/v1",V1ApiRouter(vertx,this))
        if(configProvider.get().handleStatic){
            val staticHandler =
                if (configProvider.get().useCustomStatic) StaticHandler.create(configProvider.get().customStaticDictionary)
                else StaticHandler.create()
            mainRouter.route()
                .handler(staticHandler).handlerApply { reroute("/") }
        }
        vertx.createHttpServer().requestHandler(mainRouter).listen(configProvider.get().port)
    }

    private suspend fun deployVerticles(){
        vertx.deployVerticleAwait(JsonConfigVertical(Services.CONFIG))
        vertx.deployVerticleAwait(MiraiVerticle(Services.BOT))
        vertx.deployVerticleAwait(FileVerticle(Services.FILE))
        vertx.deployVerticleAwait(LuaScriptVerticle(Services.SCRIPT))
        vertx.deployVerticleAwait(AuthVerticle(Services.AUTH))
        vertx.deployVerticleAwait(LogPublisherVerticle(Services.LOG))
        vertx.deployVerticleAwait(BotEventPublisherVerticle(Services.BOT_EVENT))
    }
}