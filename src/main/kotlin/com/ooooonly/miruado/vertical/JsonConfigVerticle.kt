package com.ooooonly.miruado.vertical

import com.ooooonly.miruado.service.JsonConfigProvider
import com.ooooonly.miruado.utils.getServiceProxy
import com.ooooonly.miruado.utils.mapToConfigObject
import com.ooooonly.vertx.kotlin.rpc.RpcCoroutineVerticle
import io.vertx.core.Verticle
import io.vertx.core.json.JsonObject
import java.io.File
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty


@Suppress("unused")
inline fun <reified T : Any> Verticle.provideConfig(serviceAddress:String) = object : ReadOnlyProperty<Any?, ConfigProvider<T>> {
    override fun getValue(thisRef: Any?, property: KProperty<*>): ConfigProvider<T> = ConfigProvider(serviceAddress,thisRef as Verticle,T::class)
}

class ConfigProvider<T : Any>(
    channel: String,
    private val verticle: Verticle,
    private val configKClass:KClass<T>
){
    private val configService by lazy { verticle.getServiceProxy<JsonConfigProvider>(channel) }
    internal var config: T? = null

    suspend fun get():T{
        config?.let { return it }
        config = configService.getConfig(verticle::class.simpleName?:"").mapToConfigObject(configKClass)
        configService.setConfig(verticle::class.simpleName?:"", JsonObject.mapFrom(config))
        return config!!
    }
}

class JsonConfigVertical(channel:String):RpcCoroutineVerticle(channel), JsonConfigProvider {
    companion object{
        const val DICTIONARY_ROOT = "/config"
        const val CONFIG_PATH = "$DICTIONARY_ROOT/config.json"
    }

    private lateinit var root: JsonObject

    override suspend fun start() {
        super.start()
        root = File(CONFIG_PATH).takeIf { it.exists() }?.let{
            try {
                JsonObject(it.readText())
            }catch (e:Exception){
                JsonObject()
            }
        }?:JsonObject()
    }

    override suspend fun getConfig(key: String): JsonObject = try {
        root.getJsonObject(key)
    } catch (e:Exception) {
        JsonObject()
    }

    override suspend fun setConfig(key: String, config: JsonObject) {
        root.put(key,config)
        File(CONFIG_PATH).writeText(root.encodePrettily())
    }

    override suspend fun getAbsoluteConfigDictionary(): String  = DICTIONARY_ROOT
}