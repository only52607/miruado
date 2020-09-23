package com.ooooonly.miruado.verticals

import com.ooooonly.luaMirai.lua.ScriptInfo
import com.ooooonly.luaMirai.lua.ScriptManager
import com.ooooonly.miruado.Config
import com.ooooonly.miruado.entities.FileInfo
import com.ooooonly.miruado.service.FileService
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.InvalidResponseException
import com.ooooonly.miruado.utils.NotFoundResponseException
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.*

class FileVertical(channel:String, private val fileDictionary: String):RpcCoroutineVerticle(channel), FileService {
    private fun getFile(fileName:String):File = File(fileDictionary, fileName)
    private suspend fun <T> ifFileExist(fileName:String, block:suspend (File)-> T ):T =
        getFile(fileName).takeIf { it.exists() }?.let { block(it) } ?: throw NotFoundResponseException("此文件不存在!")
    private suspend fun <T> ifAbsoluteFileExist(fileName:String, block:suspend (File)-> T ):T =
        File(fileName).takeIf { it.exists() }?.let { block(it) } ?: throw NotFoundResponseException("此文件不存在!")

    override suspend fun isFileExists(fileName: String): Boolean = getFile(fileName).exists()

    override suspend fun checkFileAbsolutePath(fileName: String): String = ifFileExist(fileName){
        it.absolutePath
    }

    override suspend fun getFileContentBase64(fileName: String): String = ifFileExist(fileName){
        withContext(Dispatchers.IO) {
            Base64.getEncoder().encodeToString(it.readBytes())
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

    override suspend fun getFileInfo(fileName: String): FileInfo = FileInfo.fromFile(File(Config.Upload.SCRIPTS, fileName))

    override suspend fun getAllFilesInfo(): List<FileInfo> = FileInfo.fromFiles(File(fileDictionary).listFiles())

    override suspend fun renameFile(fileName: String, newFileName: String): Unit = ifFileExist(fileName){
        it.renameTo(File(it.parentFile, newFileName))
        Unit
    }

    override suspend fun createFileFromUploads(fileName: String, uploadFileName: String,force:Boolean): Unit = ifAbsoluteFileExist(uploadFileName){
        println(uploadFileName)
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
}