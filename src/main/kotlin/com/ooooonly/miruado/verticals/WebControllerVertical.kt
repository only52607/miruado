package com.ooooonly.miruado.verticals

import com.ooooonly.luaMirai.frontend.web.utils.*
import com.ooooonly.miruado.Config
import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.AuthService
import com.ooooonly.miruado.service.BotService
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.checkResponseException
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.eventbus.EventBus
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.handler.ResponseContentTypeHandler
import io.vertx.ext.web.handler.StaticHandler
import io.vertx.kotlin.core.deployVerticleAwait
import io.vertx.kotlin.core.http.sendFileAwait
import io.vertx.kotlin.coroutines.CoroutineVerticle
import java.util.*


class WebControllerVertical(val port:Int):CoroutineVerticle() {

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
        vertx.deployVerticleAwait(BotVertical(Services.BOT,Config.Eventbus.LOGIN_SOLVER))
        vertx.deployVerticleAwait(FileVertical(Services.FILE,Config.Upload.SCRIPTS))
        vertx.deployVerticleAwait(LuaScriptVertical(Services.SCRIPT))
        vertx.deployVerticleAwait(AuthVertical(Services.AUTH))
        vertx.deployVerticleAwait(LogPublisherVertical(Services.LOG,Config.Eventbus.LOG))

        val mainRouter = vertx.createRouter()
        with(mainRouter.route()) {
            handleCors()
            handler(BodyHandler.create().setUploadsDirectory(Config.Upload.SCRIPTS).setDeleteUploadedFilesOnEnd(true))
            handler(StaticHandler.create())
            handler(ResponseContentTypeHandler.create())
            failureHandler { context ->
                println("Catch exception:")
                context.failure().checkResponseException()?.let {
                    context.response().setStatusCode(it.code).end(it.failMessage)
                    println(it.failMessage)
                }?: run {
                    context.failure().printStackTrace()
                    context.responseServerErrorEnd(context.failure().message ?: "")
                }
            }
        }
        mainRouter.route(Config.Eventbus.ROUTE).handler(buildSockJsHandler(vertx) {
            addOutboundAddressRegex(Config.Eventbus.LOG)
            addOutboundAddressRegex(Config.Eventbus.LOGIN_SOLVER)
        })
        mainRouter.route(Config.Route.API.anySubPath()).coroutineHandlerApply(this) {
            if (request().path() == Config.Route.API.subPath(Config.Route.AUTH)) return@coroutineHandlerApply next()
            authService.authCheck(request().getHeader(Config.JWT.TOKEN_KEY))
            next()
        }
        vertx.createSubRouter(mainRouter, Config.Route.API, ::api)
        vertx.createHttpServer().requestHandler(mainRouter).listen(port)
    }
    private fun api(router:Router) {
        vertx.createSubRouter(router, Config.Route.AUTH.asSubPath(), ::apiAuth)
        vertx.createSubRouter(router, Config.Route.BOTS.asSubPath(), ::apiBot)
        vertx.createSubRouter(router, Config.Route.SCRIPTS.asSubPath(), ::apiScript)
        vertx.createSubRouter(router, Config.Route.FILES.asSubPath(), ::apiFile)
        vertx.createSubRouter(router, Config.Route.COMMAND.asSubPath(), ::apiCommand)
        vertx.createSubRouter(router, Config.Route.LOGIN_SOLVER.asSubPath(), ::apiLoginSolver)
    }
    private fun apiAuth(router:Router) {
        router.handleJson()
        router.apply {
            postCoroutineHandlerApply(this@WebControllerVertical) {
                response().putHeader(Config.JWT.TOKEN_KEY,authService.generateToken(bodyAsJson))
                responseOkEnd("验证通过！")
            }
            getCoroutineHandlerApply(this@WebControllerVertical) {
                responseOkEnd(authService.getPrincipal(request().getHeader(Config.JWT.TOKEN_KEY)))
            }
        }
    }
    private fun apiBot(router:Router) {
        router.handleJson()
        router.apply {
            getCoroutineHandlerApply(ROOT,this@WebControllerVertical) {
                responseEnd(botService.getAllBotsInfo())
            }
            getCoroutineHandlerApply("/:botId",this@WebControllerVertical) {
                responseEnd(botService.getBotInfo(pathParam("botId").toLong()))
            }
            postCoroutineHandlerApply(ROOT, this@WebControllerVertical) {
                botService.createBot(getBodyAsObject())
                responseCreatedEnd("创建成功")
            }
            deleteCoroutineHandlerApply("/:botId", this@WebControllerVertical) {
                botService.deleteBot(pathParam("botId").toLong())
                responseDeletedEnd("删除成功！")
            }
        }
    }
    private fun apiScript(router:Router) {
        router.handleJson()
        router.apply {
            postCoroutineHandlerApply(ROOT,this@WebControllerVertical) {
                scriptService.addScriptFromFile(bodyAsJson.getString("name"))
                responseCreatedEnd("添加脚本成功！")
            }
            getCoroutineHandlerApply("/:index/reload", this@WebControllerVertical) {
                scriptService.reloadScript(pathParam("index").toInt())
                responseOkEnd("重载成功")
            }
            getCoroutineHandlerApply(ROOT, this@WebControllerVertical) {
                val scriptInfos = JsonArray()
                scriptService.getAllScriptsInfo().forEach {
                    scriptInfos.add(JsonObject.mapFrom(it))
                }
                responseOkEnd(scriptInfos)
            }
            deleteCoroutineHandlerApply("/:index", this@WebControllerVertical) {
                scriptService.removeScript(pathParam("index").toInt())
                responseDeletedEnd("删除成功！")
            }
        }
    }
    private fun apiFile(router: Router){
        router.apply {
            getCoroutineHandlerApply("/:filename/raw", this@WebControllerVertical) {
                responseOkEnd(String(fileService.getFileContentBase64(pathParam("filename")).bytes))
            }
            putCoroutineHandlerApply("/:filename/raw", this@WebControllerVertical) {
                fileService.setFileContentBase64(pathParam("filename"),bodyAsString)
                responseCreatedEnd("更新成功！")
            }
            putCoroutineHandlerApply("/:filename/name", this@WebControllerVertical) {
                fileService.renameFile(pathParam("filename"),JsonObject(bodyAsString).getString("name"))
                responseCreatedEnd("更新成功！")
            }
            getCoroutineHandlerApply("/:filename/file", this@WebControllerVertical) {
                response().putHeader("content-Type", "text/plain")
                response().putHeader("Content-Disposition", "attachment;filename=${pathParam("filename")}")
                response().sendFileAwait(fileService.checkFileAbsolutePath(pathParam("filename")))
                response().end()
            }
            postCoroutineHandlerApply(ROOT, this@WebControllerVertical) {
                response().isChunked = true
                for (f in fileUploads()) {
                    fileService.createFileFromUploads(f.fileName(),f.uploadedFileName(),true)
                }
                responseCreatedEnd("上传脚本成功!")
            }
            getCoroutineHandlerApply(ROOT, this@WebControllerVertical) {
                responseOkEnd(fileService.getAllFilesInfo())
            }
            getCoroutineHandlerApply("/:filename", this@WebControllerVertical) {
                responseOkEnd(fileService.getFileInfo(pathParam("filename")))
            }
            deleteCoroutineHandlerApply("/:filename", this@WebControllerVertical) {
                fileService.deleteFile(pathParam("filename"))
                responseDeletedEnd("删除脚本成功!")
            }
            putCoroutineHandlerApply("/:filename", this@WebControllerVertical) {
                fileService.createFileFromUploads(pathParam("filename"),fileUploads().first().uploadedFileName(),false)
                responseCreatedEnd("更新脚本成功!")
            }
        }
    }
    private fun apiCommand(router: Router) {
        router.handleJson()
        router.apply {
            postHandlerApply {
                val command = bodyAsJson.getString("command")
                responseOkEnd("")
            }
        }
    }
    private fun apiLoginSolver(router: Router) {
        router.handleJson()
        router.apply {
            postCoroutineHandlerApply(this@WebControllerVertical) {
                botService.finishPicCaptcha(0L,bodyAsJson.getString("result"))
                responseOkEnd("")
            }
        }
    }

//    fun periodicPublishTest() {
//        vertx.setPeriodic(1000) {
//            eventBus.publish("bot", "现在时间是：" + Date()) //发布消息
//        }
//    }
}
