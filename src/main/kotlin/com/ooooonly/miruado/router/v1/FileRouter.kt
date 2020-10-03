package com.ooooonly.miruado.router.v1

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.utils.*
import io.vertx.core.Vertx
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import io.vertx.kotlin.core.http.sendFileAwait
import kotlinx.coroutines.CoroutineScope

class FileRouter(val vertx:Vertx, scope: CoroutineScope): Router by vertx.createRouter() {
    private val fileService by vertx.provideService<FileService>(Services.FILE)
    init {
        get("/:filename/raw").responseSuspendEndWith(scope) {
            String(fileService.getFileContentBase64(pathParam("filename")).bytes)
        }
        put("/:filename/raw").responseSuspendEndWith(scope,StatusCode.CREATED) {
            fileService.setFileContentBase64(pathParam("filename"),bodyAsString)
        }
        put("/:filename/name").responseSuspendEndWith(scope,StatusCode.CREATED) {
            fileService.renameFile(pathParam("filename"), JsonObject(bodyAsString).getString("name"))
        }
        get("/:filename/file").coroutineHandlerApply(scope) {
            response.putHeader("content-Type", "text/plain")
                .putHeader("Content-Disposition", "attachment;filename=${pathParam("filename")}")
                .sendFileAwait(fileService.checkFileAbsolutePath(pathParam("filename")))
        }
        post("/").responseSuspendEndWith(scope,StatusCode.CREATED) {
            response().isChunked = true
            for (f in fileUploads()) {
                fileService.createFileFromUploads(f.fileName(),f.uploadedFileName(),true)
            }
        }
        get("/").responseSuspendEndWith(scope) {
            fileService.getAllFilesInfo()
        }
        get("/:filename").responseSuspendEndWith(scope) {
            fileService.getFileInfo(pathParam("filename"))
        }
        delete("/:filename").responseSuspendEndWith(scope,StatusCode.DELETED) {
            fileService.deleteFile(pathParam("filename"))
        }
        put("/:filename").responseSuspendEndWith(scope,StatusCode.SUCCESS) {
            fileService.createFileFromUploads(pathParam("filename"),fileUploads().first().uploadedFileName(),false)
        }
    }
}