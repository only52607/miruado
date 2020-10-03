package com.ooooonly.miruado.utils

import com.ooooonly.vertx.kotlin.rpc.getServiceProxy
import io.vertx.core.Vertx
import io.vertx.core.eventbus.EventBus
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

@Suppress("unused")
inline fun <reified T : Any> Vertx.provideService(serviceAddress:String) = object : ReadOnlyProperty<Any?, T> {
    private val service by lazy {
        getServiceProxy<T>(serviceAddress)
    }
    override fun getValue(thisRef: Any?, property: KProperty<*>): T = service
}
@Suppress("unused")
val Vertx.eventBus:EventBus
    get() = eventBus()