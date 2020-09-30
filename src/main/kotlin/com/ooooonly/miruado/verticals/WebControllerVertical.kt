package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.handler.FixCorsHandler
import com.ooooonly.miruado.handler.SinglePageStaticHandler
import com.ooooonly.miruado.service.*
import com.ooooonly.miruado.utils.*
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.ext.web.handler.sockjs.SockJSHandler
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.http.sendFileAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import io.vertx.kotlin.ext.web.handler.sockjs.permittedOptionsOf
import io.vertx.kotlin.ext.web.handler.sockjs.sockJSBridgeOptionsOf

class WebControllerVertical:CoroutineVerticle() {
    companion object{
        data class Config(
            val port: Int = 80,
            val handleStatic: Boolean = true,
            val useCustomStatic: Boolean = false,
            val customStaticDictionary: String = "",
            val eventBusPublishAddressRegex: String = "sockJs.+",
            val indexPageFile: String = "/webroot/index.html",
            val tokenKey: String = "Authorization"
        )
    }

    private val botService by lazy { vertx.getServiceProxy<BotService>(Services.BOT) }
    private val fileService by lazy { vertx.getServiceProxy<FileService>(Services.FILE) }
    private val scriptService by lazy { vertx.getServiceProxy<ScriptService>(Services.SCRIPT) }
    private val authService by lazy { vertx.getServiceProxy<AuthService>(Services.AUTH) }
    private val configService by lazy { vertx.getServiceProxy<JsonConfigProvider>(Services.CONFIG) }

    private lateinit var verticalConfig:Config

    override suspend fun start() {
        vertx.deployVerticleAwait(JsonConfigVertical(Services.CONFIG))
        vertx.deployVerticleAwait(BotVertical(Services.BOT))
        vertx.deployVerticleAwait(FileVertical(Services.FILE))
        vertx.deployVerticleAwait(LuaScriptVertical(Services.SCRIPT))
        vertx.deployVerticleAwait(AuthVertical(Services.AUTH))
        vertx.deployVerticleAwait(LogPublisherVertical(Services.LOG))

        verticalConfig = configService.getConfig("web").mapToConfigObject()
        configService.setConfig("web",JsonObject.mapFrom(verticalConfig))

        val mainRouter = vertx.createRouter()
        mainRouter.route()
            .handler(FixCorsHandler(verticalConfig.tokenKey))
            .handler(BodyHandler.create().setUploadsDirectory(fileService.getUploadPath()).setDeleteUploadedFilesOnEnd(true))
            .handler(ResponseContentTypeHandler.create())
            .failureHandler(ResponseException.failureHandler)
        mainRouter.mountSubRouter("/eb",SockJSHandler.create(vertx).bridge(
            sockJSBridgeOptionsOf(outboundPermitted = listOf(permittedOptionsOf(addressRegex = verticalConfig.eventBusPublishAddressRegex)) )
        ))
        mainRouter.mountSubRouter("/api/v1",apiRouter)
        if(verticalConfig.handleStatic){
            mainRouter.route()
                .handler(StaticHandler.create())
                .handler(SinglePageStaticHandler.create(javaClass.getResource(verticalConfig.indexPageFile).path))
        }
        vertx.createHttpServer().requestHandler(mainRouter).listen(verticalConfig.port)
    }

    private val apiRouter get() = vertx.createRouter().apply {
        route().handlerPreApply { response.putHeader("Content-Type","application/json") }
        mountSubRouter("/auth",authRouter)
        route().coroutineHandlerPreApply(this@WebControllerVertical){
            authService.authCheck(request.getHeader(verticalConfig.tokenKey))
        }
        mountSubRouter("/bots", botRouter)
        mountSubRouter("/scripts", scriptRouter)
        mountSubRouter("/files", fileRouter)
    }

    private val authRouter get() = vertx.createRouter().apply {
        post().responseSuspendEndWith(this@WebControllerVertical,StatusCode.SUCCESS) {
            response.putHeader(verticalConfig.tokenKey,authService.generateToken(bodyAsJson))
        }
        get().responseSuspendEndWith(this@WebControllerVertical) {
            authService.getPrincipal(request.getHeader(verticalConfig.tokenKey))
        }
    }

    private val botRouter get() = vertx.createRouter().apply {
        get("/").responseSuspendEndWith(this@WebControllerVertical){
            botService.getAllBotsInfo()
        }
        get("/:botId").responseSuspendEndWith(this@WebControllerVertical) {
            botService.getBotInfo(pathParam("botId").toLong())
        }
        post("/").responseSuspendEndWith(this@WebControllerVertical,StatusCode.CREATED) {
            botService.createBot(getBodyAsObject())
        }
        delete("/:botId").responseSuspendEndWith(this@WebControllerVertical,StatusCode.DELETED) {
            botService.deleteBot(pathParam("botId").toLong())
        }
        post("/:botId/captchaResult").responseSuspendEndWith(this@WebControllerVertical,StatusCode.SUCCESS) {
            botService.finishPicCaptcha(pathParam("botId").toLong(),bodyAsJson.getString("result"))
        }
    }

    private val scriptRouter get() = vertx.createRouter().apply {
        post("/").responseSuspendEndWith(this@WebControllerVertical,StatusCode.CREATED) {
            scriptService.addScriptFromFile(bodyAsJson.getString("name"))
        }
        get("/:index/reload").responseSuspendEndWith(this@WebControllerVertical,StatusCode.SUCCESS) {
            scriptService.reloadScript(pathParam("index").toInt())
        }
        get("/").responseSuspendEndWith(this@WebControllerVertical) {
            JsonArray().also { scriptInfos ->
                scriptService.getAllScriptsInfo().forEach {
                    scriptInfos.add(JsonObject.mapFrom(it))
                }
            }
        }
        delete("/:index").responseSuspendEndWith(this@WebControllerVertical,StatusCode.DELETED) {
            scriptService.removeScript(pathParam("index").toInt())
        }
    }

    private val fileRouter get() = vertx.createRouter().apply {
        get("/:filename/raw").responseSuspendEndWith( this@WebControllerVertical) {
            String(fileService.getFileContentBase64(pathParam("filename")).bytes)
        }
        put("/:filename/raw").responseSuspendEndWith( this@WebControllerVertical,StatusCode.CREATED) {
            fileService.setFileContentBase64(pathParam("filename"),bodyAsString)
        }
        put("/:filename/name").responseSuspendEndWith(this@WebControllerVertical,StatusCode.CREATED) {
            fileService.renameFile(pathParam("filename"),JsonObject(bodyAsString).getString("name"))
        }
        get("/:filename/file").coroutineHandlerApply(this@WebControllerVertical) {
            response.putHeader("content-Type", "text/plain")
                .putHeader("Content-Disposition", "attachment;filename=${pathParam("filename")}")
                .sendFileAwait(fileService.checkFileAbsolutePath(pathParam("filename")))
        }
        post("/").responseSuspendEndWith( this@WebControllerVertical,StatusCode.CREATED) {
            response().isChunked = true
            for (f in fileUploads()) {
                fileService.createFileFromUploads(f.fileName(),f.uploadedFileName(),true)
            }
        }
        get("/").responseSuspendEndWith( this@WebControllerVertical) {
            fileService.getAllFilesInfo()
        }
        get("/:filename").responseSuspendEndWith( this@WebControllerVertical) {
            fileService.getFileInfo(pathParam("filename"))
        }
        delete("/:filename").responseSuspendEndWith( this@WebControllerVertical,StatusCode.DELETED) {
            fileService.deleteFile(pathParam("filename"))
        }
        put("/:filename").responseSuspendEndWith( this@WebControllerVertical,StatusCode.SUCCESS) {
            fileService.createFileFromUploads(pathParam("filename"),fileUploads().first().uploadedFileName(),false)
        }
    }
}