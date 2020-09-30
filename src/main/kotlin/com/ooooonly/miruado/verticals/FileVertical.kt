package com.ooooonly.miruado.verticals

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.entities.FileInfo
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.miruado.utils.mapToConfigObject
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.buffer.Buffer
import io.vertx.core.json.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FileVertical(channel:String):RpcCoroutineVerticle(channel), FileService {
    companion object{
        data class Config(
            val uploadPath:String = "/scripts"
        )
    }

    private val configService by lazy { vertx.getServiceProxy<JsonConfigProvider>(Services.CONFIG) }
    private lateinit var verticalConfig: Config

    private lateinit var absoluteUploadPath:String

    override suspend fun start() {
        super.start()
        verticalConfig = configService.getConfig("file").mapToConfigObject()
        configService.setConfig("file",JsonObject.mapFrom(verticalConfig))
        absoluteUploadPath = configService.getAbsoluteConfigDictionary() + verticalConfig.uploadPath
    }

    private fun getFile(fileName:String):File = File(absoluteUploadPath, fileName)
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

    override suspend fun setFileContentBase64(fileName: String, base64Content: String){
        getFile(fileName).let{
            withContext(Dispatchers.IO) {
                FileOutputStream(it).apply {
                    write(Base64.getDecoder().decode(base64Content))
                }.close()
            }
        }
    }

    override suspend fun getFileInfo(fileName: String): FileInfo = FileInfo.fromFile(getFile(fileName))

    override suspend fun getAllFilesInfo(): List<FileInfo> = FileInfo.fromFiles(File(absoluteUploadPath).listFiles()!!)

    override suspend fun renameFile(fileName: String, newFileName: String) {
        ifFileExist(fileName){
            it.renameTo(File(it.parentFile, newFileName))
        }
    }

    override suspend fun createFileFromUploads(fileName: String, uploadFileName: String,force:Boolean){
        ifAbsoluteFileExist(uploadFileName){
            val newFile = File(it.parentFile, fileName)
            if (force && newFile.exists()){
                newFile.delete()
            }
            it.renameTo(newFile)
        }
    }

    override suspend fun deleteFile(fileName: String) {
        ifFileExist(fileName){
            it.delete()
        }
    }

    override suspend fun getUploadPath(): String = absoluteUploadPath
}