/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.client

import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.injection.bootstrap.ClassInventory
import java.lang.reflect.Modifier

object ClassUtils {

    private val cachedClasses = mutableMapOf<String, Boolean>()

    /**
     * Allows you to check for existing classes with the [className]
     */
    fun hasClass(className: String) =
        if (className in cachedClasses)
            cachedClasses.getValue(className)
        else try {
            Class.forName(className)
            cachedClasses[className] = true

            true
        } catch (e: ClassNotFoundException) {
            cachedClasses[className] = false

            false
        }


    fun getObjectInstance(clazz: Class<*>): Any {
        clazz.declaredFields.forEach {
            if (it.name == "INSTANCE") {
                return it.get(null)
            }
        }
        throw IllegalAccessException("This class not a kotlin object")
    }

    fun getValues(clazz: Class<*>, instance: Any) = clazz.declaredFields.map { valueField ->
        valueField.isAccessible = true
        valueField[instance]
    }.filterIsInstance<Value<*>>()

    /**
     * Scan classes with a specified superclass using the owning class loader's package inventory.
     * @author liulihaocai
     */
    fun <T : Any> resolvePackage(packagePath: String, klass: Class<T>): List<Class<out T>> {
        // use a list to cache classes
        val list = mutableListOf<Class<out T>>()

        for (name in ClassInventory.names(klass.classLoader, packagePath)) {
            val resolved = Class.forName(name, false, klass.classLoader)
            resolved.declaredMethods.find {
                Modifier.isNative(it.modifiers)
            }?.let {
                val klass1 = it.declaringClass.typeName+"."+it.name
                throw UnsatisfiedLinkError(klass1+"\n\tat ${klass1}(Native Method)") // we don't want native methods
            }
            // check if class is assignable from target class
            if(klass.isAssignableFrom(resolved) && !resolved.isInterface && !Modifier.isAbstract(resolved.modifiers)) {
                // add to list
                list.add(resolved.asSubclass(klass))
            }
        }

        return list
    }

    /**
     * Useful in preventing the config system from reading the given [value]
     */
    fun checkIfExcluded(list: MutableSet<Value<*>>, value: Value<*>) {
        if (value.excluded) {
            return
        }
        list += value
    }

    fun hasForge() = hasClass("net.minecraftforge.common.MinecraftForge")

    val Any.debugString: String
        get() = this::class.java.declaredFields.joinToString(
            separator = ", ",
            prefix = "${this::class.java.simpleName}(",
            postfix = ")"
        ) { property ->
            property.isAccessible = true
            val name = property.name
            val value = property.get(this)
            "$name=$value"
        }
}
