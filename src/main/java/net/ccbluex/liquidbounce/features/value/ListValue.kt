package net.ccbluex.liquidbounce.features.value

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.util.*

/**
 * List value represents a selectable list of values
 */
open class ListValue(name: String, val values: Array<String>, value: String) : Value<String>(name, value) {
    @JvmField
    var openList = false
    fun getModeListNumber(mode: String) = values.indexOf(mode)
    init {
        this.value = value
    }

    init {
        this.value = value
    }

    open fun getModes(): List<String?>? {
        return values.toList()
    }
    init {
        this.value = value
    }

    open fun getModeGet(i: Int): String? {
        return values[i]
    }

    operator fun contains(string: String?): Boolean {
        return Arrays.stream(values).anyMatch { s: String -> s.equals(string, ignoreCase = true) }
    }

    fun indexOf(mode: String): Int {
        for (i in values.indices) {
            if (values[i].equals(mode, true)) return i
        }
        return 0
    }

    fun isMode(string: String): Boolean {
        return this.value.equals(string, ignoreCase = true)
    }

    fun containsValue(string: String): Boolean {
        return Arrays.stream(values).anyMatch { it.equals(string, ignoreCase = true) }
    }

    override fun changeValue(value: String) {
        for (element in values) {
            if (element.equals(value, ignoreCase = true)) {
                this.value = element
                break
            }
        }
    }

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) changeValue(element.asString)
    }
}


