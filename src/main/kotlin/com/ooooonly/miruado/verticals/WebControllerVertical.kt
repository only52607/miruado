package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.*
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.service.BotService
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.ScriptService
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

class WebControllerVertical(private var port:Int = 80,private var handleStatic:Boolean = true):CoroutineVerticle() {
    companion object{
        const val TOKEN_KEY = "Authorization"
        val uploadPath = DICTIONARY_ROOT + globalConfig.getString("upload")
    }
    private val botService:BotService by lazy {
        vertx.getServiceProxy<BotService>(Services.BOT)
    }
    private val fileService:FileService by lazy {
        vertx.getServiceProxy<FileService>(Services.FILE)
    }
    private val scriptService:ScriptService by lazy {
        vertx.getServiceProxy<ScriptService>(Services.SCRIPT)
    }
    private val authService: AuthService by lazy {
        vertx.getServiceProxy<AuthService>(Services.AUTH)
    }
    override suspend fun start() {
        vertx.deployVerticleAwait(BotVertical(Services.BOT,"eventBus.bot.loginSolver"))
        vertx.deployVerticleAwait(FileVertical(Services.FILE, uploadPath))
        vertx.deployVerticleAwait(LuaScriptVertical(Services.SCRIPT))
        vertx.deployVerticleAwait(AuthVertical(Services.AUTH))
        vertx.deployVerticleAwait(LogPublisherVertical(Services.LOG,"eventBus.bot.log"))

        val mainRouter = vertx.createRouter()
        mainRouter.route()
            .handler(FixCorsHandler(TOKEN_KEY))
            .handler(BodyHandler.create().setUploadsDirectory(uploadPath).setDeleteUploadedFilesOnEnd(true))
            .handler(ResponseContentTypeHandler.create())
            .failureHandler(ResponseException.failureHandler)
        mainRouter.route(Routes.API + "/*").coroutineHandlerApply(this) {
            if (request().path() == Routes.API + Routes.AUTH) return@coroutineHandlerApply next()
            authService.authCheck(request().getHeader(TOKEN_KEY))
            response().putHeader("Content-Type","application/json")
            next()
        }
        mainRouter.mountSubRouter(Routes.EVENT_BUS,SockJSHandler.create(vertx).bridge(
            sockJSBridgeOptionsOf(outboundPermitted = listOf(permittedOptionsOf(addressRegex = "eventBus.+")) )
        ))
        mainRouter.mountSubRouter(Routes.API,apiRouter)
        if(handleStatic){
            mainRouter.route()
                .handler(StaticHandler.create())
                .handler(SinglePageStaticHandler.create(javaClass.getResource("/webroot/index.html").path))
        }
        vertx.createHttpServer().requestHandler(mainRouter).listen(port)
    }

    private val apiRouter get() = vertx.createRouter().apply {
        mountSubRouter(Routes.AUTH,authRouter)
        mountSubRouter(Routes.AUTH, authRouter)
        mountSubRouter(Routes.BOTS, botRouter)
        mountSubRouter(Routes.SCRIPTS, scriptRouter)
        mountSubRouter(Routes.FILES, fileRouter)
    }

    private val authRouter get() = vertx.createRouter().apply {
        post().responseSuspendEndWith(this@WebControllerVertical,StatusCode.SUCCESS) {
            response().putHeader(TOKEN_KEY,authService.generateToken(bodyAsJson))
        }
        get().responseSuspendEndWith(this@WebControllerVertical) {
            authService.getPrincipal(request().getHeader(TOKEN_KEY))
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
            response().putHeader("content-Type", "text/plain")
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
