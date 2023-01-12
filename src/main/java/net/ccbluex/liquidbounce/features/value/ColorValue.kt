package net.ccbluex.liquidbounce.features.value

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

open class  ColorValue(name : String, value: Int) : Value<Int>(name, value) {
    open fun getValue(): Int {
        return super.get()
    }

    fun set(newValue: Number) {
        set(newValue.toInt())
    }


    override fun toJson() = JsonPrimitive(getValue())

    override fun fromJson(element: JsonElement) {
        if(element.isJsonPrimitive)
            value = element.asInt
    }

    open fun getHSB(): FloatArray {
        val hsbValues = FloatArray(3)
        val saturation: Float
        val brightness: Float
        var hue: Float
        var cMax: Int = (getValue() ushr 16 and 0xFF).coerceAtLeast(getValue() ushr 8 and 0xFF)
        if (getValue() and 0xFF > cMax) cMax = getValue() and 0xFF
        var cMin: Int = (getValue() ushr 16 and 0xFF).coerceAtMost(getValue() ushr 8 and 0xFF)
        if (getValue() and 0xFF < cMin) cMin = getValue() and 0xFF
        brightness = cMax.toFloat() / 255.0f
        saturation = if (cMax != 0) (cMax - cMin).toFloat() / cMax.toFloat() else 0F
        if (saturation == 0f) {
            hue = 0f
        } else {
            val redC: Float = (cMax - (getValue() ushr 16 and 0xFF)).toFloat() / (cMax - cMin).toFloat()
            // @off
            val greenC: Float = (cMax - (getValue() ushr 8 and 0xFF)).toFloat() / (cMax - cMin).toFloat()
            val blueC: Float = (cMax - (getValue() and 0xFF)).toFloat() / (cMax - cMin).toFloat() // @on
            hue =
                (if (getValue() ushr 16 and 0xFF == cMax) blueC - greenC else if (getValue() ushr 8 and 0xFF == cMax) 2.0f + redC - blueC else 4.0f + greenC - redC) / 6.0f
            if (hue < 0) hue += 1.0f
        }
        hsbValues[0] = hue
        hsbValues[1] = saturation
        hsbValues[2] = brightness
        return hsbValues
    }


}
