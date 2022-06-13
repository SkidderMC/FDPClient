package net.ccbluex.liquidbounce.value

import net.ccbluex.liquidbounce.launch.data.legacyui.clickgui.AnimationHelper
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

/**
 * Bool value represents a value with a boolean
 */
open class BoolValue(name: String, value: Boolean) : Value<Boolean>(name, value) {

    var anim = 0F
    val animation = AnimationHelper(this)

    override fun toJson() = JsonPrimitive(value)

    override fun fromJson(element: JsonElement) {
        if (element.isJsonPrimitive) {
            value = element.asBoolean || element.asString.equals("true", ignoreCase = true)

            animation.animationX = if(value) 5F else -5F
        }

         fun toggle(){
            this.value = !this.value
        }
    }

}