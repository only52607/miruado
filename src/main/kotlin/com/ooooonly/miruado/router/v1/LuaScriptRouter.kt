package com.ooooonly.miruado.router.v1

import com.ooooonly.miruado.Services
import com.ooooonly.miruado.service.ScriptService
import com.ooooonly.miruado.utils.StatusCode
import com.ooooonly.miruado.utils.createRouter
import com.ooooonly.miruado.utils.provideService
import com.ooooonly.miruado.utils.responseSuspendEndWith
import io.vertx.core.Vertx
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import io.vertx.ext.web.Router
import kotlinx.coroutines.CoroutineScope

class LuaScriptRouter(val vertx: Vertx, scope: CoroutineScope): Router by vertx.createRouter() {
    private val scriptService by vertx.provideService<ScriptService>(Services.SCRIPT)
    init {
        post("/").responseSuspendEndWith(scope,StatusCode.CREATED) {
            scriptService.addScriptFromFile(bodyAsJson.getString("name"))
        }
        get("/:index/reload").responseSuspendEndWith(scope,StatusCode.SUCCESS) {
            scriptService.reloadScript(pathParam("index").toInt())
        }
        get("/").responseSuspendEndWith(scope) {
            JsonArray().also { scriptInfos ->
                scriptService.getAllScriptsInfo().forEach {
                    scriptInfos.add(JsonObject.mapFrom(it))
                }
            }
        }
        delete("/:index").responseSuspendEndWith(scope,StatusCode.DELETED) {
            scriptService.removeScript(pathParam("index").toInt())
        }
    }
}