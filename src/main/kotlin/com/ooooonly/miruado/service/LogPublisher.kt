package com.ooooonly.miruado.service

interface LogPublisher {
    fun publishLog(logString: String)
    fun publishNetLog(fromBotId:Long,message:String)
    fun publishBotLog(fromBotId:Long,message:String)
}