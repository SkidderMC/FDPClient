/*
 * ZAVZ Hacked Client
 */
package me.zywl.fdpclient.value.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zywl.fdpclient.value.Value

open class NumberValue(name: String, value: Double, val minimum: Double = 0.0, val maximum: Double = Double.MAX_VALUE,val inc: Double/* = 1.0*/)
    : Value<Double>(name, value) {

    fun set(newValue: Number) {
        set(newValue.toDouble())
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive)
            value = element.asDouble
    }

    fun append(o: Double): NumberValue {
        set(get() + o)
        return this
    }
}