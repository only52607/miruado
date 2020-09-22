package com.ooooonly.miruado.mirai

import kotlinx.coroutines.CompletableDeferred

object CaptchaReceiver {
    private lateinit var result: CompletableDeferred<String>
    fun resetResult() {
        result = CompletableDeferred()
    }

    fun setResult(resultString: String) {
        result.complete(resultString)
    }

    suspend fun awaitResult(): String {
        return result.await()
    }
}