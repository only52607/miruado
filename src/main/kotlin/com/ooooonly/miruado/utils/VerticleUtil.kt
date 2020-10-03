package com.ooooonly.miruado.utils

import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Verticle
import io.vertx.core.eventbus.EventBus
import io.vertx.kotlin.coroutines.CoroutineVerticle
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("unused")
val Verticle.eventBus:EventBus
    get() = vertx.eventBus()

@Suppress("unused")
inline fun <reified T:Any> Verticle.getServiceProxy(channel:String) = vertx.getServiceProxy<T>(channel)

@Suppress("unused")
inline fun <reified T : Any> Verticle.provideService(serviceAddress:String) = object : ReadOnlyProperty<Any?, T> {
    private val service by lazy {
        getServiceProxy<T>(serviceAddress)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = service
}