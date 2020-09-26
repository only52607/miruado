package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.DICTIONARY_ROOT
import com.ooooonly.miruado.entities.FileInfo
import com.ooooonly.miruado.getGlobalConfig
import com.ooooonly.miruado.getOrSetDefault
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FileVertical(channel:String):RpcCoroutineVerticle(channel), FileService {

    private var uploadPath = "$DICTIONARY_ROOT/scripts"

    override suspend fun start() {
        super.start()
        val rootConfig = vertx.getGlobalConfig()
        val fileConfig = rootConfig.getOrSetDefault("file", JsonObject())
        uploadPath = DICTIONARY_ROOT + fileConfig.getOrSetDefault("uploadPath","/scripts")
        rootConfig.put("file",fileConfig)
    }


    private fun getFile(fileName:String):File = File(uploadPath, fileName)
    private suspend fun <T> ifFileExist(fileName:String, block:suspend (File)-> T ):T =
        getFile(fileName).takeIf { it.exists() }?.let { block(it) } ?: throw NotFoundResponseException()
    private suspend fun <T> ifAbsoluteFileExist(fileName:String, block:suspend (File)-> T ):T =
        File(fileName).takeIf { it.exists() }?.let { block(it) } ?: throw NotFoundResponseException()

    override suspend fun isFileExists(fileName: String): Boolean = getFile(fileName).exists()

    override suspend fun checkFileAbsolutePath(fileName: String): String = ifFileExist(fileName){
        it.absolutePath
    }

    override suspend fun getFileContentBase64(fileName: String): Buffer = ifFileExist(fileName){
        withContext(Dispatchers.IO) {
            Buffer.buffer(Base64.getEncoder().encodeToString(it.readBytes()))
        }
    }

    override suspend fun setFileContentBase64(fileName: String, base64Content: String): Unit = getFile(fileName).let{
        withContext(Dispatchers.IO) {
            FileOutputStream(it).apply {
                write(Base64.getDecoder().decode(base64Content))
            }.close()
        }
        Unit
    }

    override suspend fun getFileInfo(fileName: String): FileInfo = FileInfo.fromFile(getFile(fileName))

    override suspend fun getAllFilesInfo(): List<FileInfo> = FileInfo.fromFiles(File(uploadPath).listFiles()!!)

    override suspend fun renameFile(fileName: String, newFileName: String): Unit = ifFileExist(fileName){
        it.renameTo(File(it.parentFile, newFileName))
        Unit
    }

    override suspend fun createFileFromUploads(fileName: String, uploadFileName: String,force:Boolean): Unit = ifAbsoluteFileExist(uploadFileName){
        val newFile = File(it.parentFile, fileName)
        if (force && newFile.exists()){
            newFile.delete()
        }
        it.renameTo(newFile)
        Unit
    }

    override suspend fun deleteFile(fileName: String): Unit = ifFileExist(fileName){
        it.delete()
        Unit
    }

    override suspend fun getUploadPath(): String = uploadPath
}