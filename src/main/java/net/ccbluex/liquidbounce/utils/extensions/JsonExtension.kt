package net.ccbluex.liquidbounce.utils.extensions

import com.google.gson.JsonElement
import com.google.gson.JsonObject

operator fun JsonObject.set(s: String, value: String) {
    this.addProperty(s, value)
}

operator fun JsonObject.set(s: String, value: Number) {
    this.addProperty(s, value)
}

operator fun JsonObject.set(s: String, value: Char) {
    this.addProperty(s, value)
}

operator fun JsonObject.set(s: String, value: Boolean) {
    this.addProperty(s, value)
}

operator fun JsonObject.set(s: String, value: JsonElement) {
    this.add(s, value)
}