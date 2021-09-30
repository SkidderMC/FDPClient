/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.value.Value

object ClassUtils {

    private val cachedClasses = mutableMapOf<String, Boolean>()

    /**
     * Allows you to check for existing classes with the [className]
     */
    @JvmStatic
    fun hasClass(className: String): Boolean {
        return if (cachedClasses.containsKey(className))
            cachedClasses[className]!!
        else try {
            Class.forName(className)
            cachedClasses[className] = true

            true
        } catch (e: ClassNotFoundException) {
            cachedClasses[className] = false

            false
        }
    }

    @JvmStatic
    fun getObjectInstance(clazz: Class<*>):Any{
        clazz.declaredFields.forEach {
            if(it.name.equals("INSTANCE")){
                return it.get(null)
            }
        }
        throw IllegalAccessException("This class not a kotlin object")
    }

    @JvmStatic
    fun getValues(clazz: Class<*>, instance: Any) = clazz.declaredFields.map { valueField ->
        valueField.isAccessible = true
        valueField[instance]
    }.filterIsInstance<Value<*>>()
}