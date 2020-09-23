package com.ooooonly.miruado.service

import com.ooooonly.miruado.entities.FileInfo

interface FileService {
    suspend fun isFileExists(fileName:String):Boolean
    suspend fun checkFileAbsolutePath(fileName:String):String
    suspend fun getFileContentBase64(fileName:String):String
    suspend fun setFileContentBase64(fileName:String,base64Content:String)
    suspend fun getFileInfo(fileName:String): FileInfo
    suspend fun getAllFilesInfo(): List<FileInfo>
    suspend fun renameFile(fileName:String,newFileName:String)
    suspend fun createFileFromUploads(fileName: String,uploadFileName:String,force:Boolean)
    suspend fun deleteFile(fileName: String)
}