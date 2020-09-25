package com.ooooonly.miruado.service

interface LogPublisher {
    suspend fun publishLog(logString: String)
    suspend fun publishNetLog(fromBotId:Long,message:String)
    suspend fun publishBotLog(fromBotId:Long,message:String)
}