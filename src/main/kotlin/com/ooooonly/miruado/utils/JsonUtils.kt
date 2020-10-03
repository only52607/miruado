package com.ooooonly.miruado.utils

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.vertx.core.json.JsonArray
import io.vertx.core.json.JsonObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KClass
import kotlin.reflect.KProperty

@Suppress("UNCHECKED_CAST","unused")
fun <T> JsonObject.getOrSetDefault(key:String, defaultValue:T):T = when(defaultValue){
    is Int -> (getInteger(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is Number -> (getNumber(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is String -> (getString(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is Boolean -> (getBoolean(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is ByteArray -> (getBinary(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is JsonArray -> (getJsonArray(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    is JsonObject -> (getJsonObject(key) as T?) ?:defaultValue.also { put(key,defaultValue) } as T
    else -> throw Exception("Unknown class!")
}

@Suppress("unused")
fun <T> JsonObject.byDefault(defaultValue:T): ReadWriteProperty<Any?, T> {
    return object : ReadWriteProperty<Any?, T> {
        override fun getValue(thisRef: Any?, property: KProperty<*>): T {
            return getOrSetDefault(property.name,defaultValue)
        }
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
            put(property.name,value)
        }
    }
}

@Suppress("unused")
fun <T> ValueWrapper<JsonObject>.byDefault(defaultValue:T): ReadWriteProperty<Any?, ValueWrapper<T>> {
    return object : ReadWriteProperty<Any?, ValueWrapper<T>> {
        var wrapper:ValueWrapper<T>?= null
        var propertyName:String = ""
        override fun getValue(thisRef: Any?, property: KProperty<*>): ValueWrapper<T> {
            val value = this@byDefault.value.getOrSetDefault(property.name,defaultValue)
            return wrapper?.apply { this.value = value }?:ValueWrapper(value).also { newWrapper ->
                wrapper = newWrapper
                propertyName = property.name
                this@byDefault.addSetterObserver {_,newValue ->
                    wrapper!!.value = newValue.getOrSetDefault(propertyName,defaultValue)
                }
            }
        }
        override fun setValue(thisRef: Any?, property: KProperty<*>, value: ValueWrapper<T>) {
            this@byDefault.value.put(property.name,value.value)
        }
    }
}

@Suppress("unused")
class DefaultConfigObjectMapper : ObjectMapper() {
    init {
        configure(DeserializationFeature.FAIL_ON_MISSING_CREATOR_PROPERTIES,false)
        configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false)
    }
}
@Suppress("unused")
inline fun <reified T> JsonObject.mapToObject(objectMapper: ObjectMapper): T = objectMapper.readValue(this.encode(),T::class.java)
@Suppress("unused")
inline fun <reified T> JsonObject.mapToConfigObject(): T = DefaultConfigObjectMapper().readValue(this.encode(),T::class.java)
@Suppress("unused")
inline fun <T : Any> JsonObject.mapToConfigObject(kclazz: KClass<T>): T = DefaultConfigObjectMapper().readValue(this.encode(),kclazz.java)