/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.value

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

/**
 * Text value represents a value with a string
 */
open class KeyValue(name: String, value: Int) : Value<Int>(name, value) {
    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asInt
        }
    }
    fun set(newValue: Number) {
        set(newValue.toInt())
    }
}