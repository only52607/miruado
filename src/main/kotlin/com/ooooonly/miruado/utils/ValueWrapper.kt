package com.ooooonly.miruado.utils
@Suppress("unused")
class ValueWrapper<T>(initValue:T){
    private var setterObservers:MutableList<((originValue:T,newValue:T)->Unit)> = mutableListOf()
    var value:T = initValue
        set(value) {
            setterObservers.forEach { it.invoke(field,value) }
            field = value
        }
    override fun toString(): String = value.toString()
    internal fun addSetterObserver(observer: ((originValue:T,newValue:T)->Unit)) = setterObservers.add(observer)
}