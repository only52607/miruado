package com.ooooonly.miruado.entity

import net.mamoe.mirai.utils.secondsToMillis

data class BotCreateInfo(
    val id: Long = 0,
    val password: String = "",
    val device: String = "/device.json",
    var heartbeatTimeoutMillis: Long = 5.secondsToMillis,
    var firstReconnectDelayMillis: Long = 5.secondsToMillis,
    var reconnectPeriodMillis: Long = 5.secondsToMillis,
    var reconnectionRetryTimes: Int = Int.MAX_VALUE,
    var protocol: String = "ANDROID_PHONE"
)