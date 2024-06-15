/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package me.zywl.fdpclient.value.impl

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import me.zywl.fdpclient.value.Value

/**
 * Text value represents a value with a string
 */
open class TextValue(name: String, value: String) : Value<String>(name, value) {
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asString
        }

    }
    fun append(o: Any): TextValue {
        set(get() + o)
        return this
    }

}