/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.file.gson

import com.google.gson.*
import com.google.gson.reflect.TypeToken
import net.ccbluex.liquidbounce.file.FileManager.PRETTY_GSON
import java.io.Reader

private val EMPTY_JSON_ARRAY = JsonArray()

private val EMPTY_JSON_OBJECT = JsonObject()

class JsonObjectBuilder {
    private val backend = JsonObject()

    infix fun String.to(value: JsonElement) {
        backend.add(this, value)
    }

    infix fun String.to(value: Char) {
        backend.addProperty(this, value)
    }

    infix fun String.to(value: Number) {
        backend.addProperty(this, value)
    }

    infix fun String.to(value: String) {
        backend.addProperty(this, value)
    }

    infix fun String.to(value: Boolean) {
        backend.addProperty(this, value)
    }

    /**
     * Fallback
     */
    infix fun String.to(value: Any?) {
        when (value) {
            null -> backend.add(this, JsonNull.INSTANCE)
            is String -> backend.addProperty(this, value)
            is Number -> backend.addProperty(this, value)
            is Boolean -> backend.addProperty(this, value)
            is JsonElement -> backend.add(this, value)
            is JsonObjectBuilder -> backend.add(this, value.build())
            else -> throw IllegalArgumentException("Unsupported type: ${value::class.java}")
        }
    }

    fun build() = backend
}

class JsonArrayBuilder {
    private val backend = JsonArray()

    operator fun JsonElement.unaryPlus() {
        backend.add(this)
    }

    fun build() = backend
}

fun json(): JsonObject = EMPTY_JSON_OBJECT

inline fun json(builderAction: JsonObjectBuilder.() -> Unit): JsonObject {
    return JsonObjectBuilder().apply(builderAction).build()
}

fun jsonArray(): JsonArray = EMPTY_JSON_ARRAY

inline fun jsonArray(builderAction: JsonArrayBuilder.() -> Unit): JsonArray {
    return JsonArrayBuilder().apply(builderAction).build()
}

inline fun <reified T> JsonElement.decode(gson: Gson = PRETTY_GSON): T = gson.fromJson(this, object : TypeToken<T>() {}.type)

inline fun <reified T> Reader.decodeJson(gson: Gson = PRETTY_GSON): T = gson.fromJson(this, object : TypeToken<T>() {}.type)

inline fun <reified T> String.decodeJson(gson: Gson = PRETTY_GSON): T = gson.fromJson(this, object : TypeToken<T>() {}.type)