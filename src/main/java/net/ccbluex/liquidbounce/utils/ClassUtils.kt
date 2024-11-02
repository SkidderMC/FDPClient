/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.utils.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.value.Value
import org.apache.logging.log4j.core.config.plugins.ResolverUtil
import java.lang.reflect.Modifier

object ClassUtils {

    private val cachedClasses = mutableMapOf<String, Boolean>()

    /**
     * Allows you to check for existing classes with the [className]
     */
    fun hasClass(className: String) =
        if (className in cachedClasses)
            cachedClasses[className]!!
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
            if (it.name.equals("INSTANCE")) {
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
     * scan classes with specified superclass like what Reflections do but with log4j [ResolverUtil]
     * @author liulihaocai
     */
    fun <T : Any> resolvePackage(packagePath: String, klass: Class<T>): List<Class<out T>> {
        // use resolver in log4j to scan classes in target package
        val resolver = ResolverUtil()

        // set class loader
        resolver.classLoader = klass.classLoader

        // set package to scan
        resolver.findInPackage(object : ResolverUtil.ClassTest() {
            override fun matches(type: Class<*>): Boolean {
                return true
            }
        }, packagePath)

        // use a list to cache classes
        val list = mutableListOf<Class<out T>>()

        for(resolved in resolver.classes) {
            resolved.declaredMethods.find {
                Modifier.isNative(it.modifiers)
            }?.let {
                val klass1 = it.declaringClass.typeName+"."+it.name
                throw UnsatisfiedLinkError(klass1+"\n\tat ${klass1}(Native Method)") // we don't want native methods
            }
            // check if class is assignable from target class
            if(klass.isAssignableFrom(resolved) && !resolved.isInterface && !Modifier.isAbstract(resolved.modifiers)) {
                // add to list
                list.add(resolved as Class<out T>)
            }
        }

        return list
    }

    fun findValues(
        element: Any?, configurables: List<Class<*>>, orderedValues: MutableSet<Value<*>>,
    ) {
        if (element == null) return

        val list = mutableSetOf<Value<*>>()

        try {
            if (element::class.java in configurables) {
                /**
                 * For variables that hold a list of Value<*>
                 *
                 * Example: val variable: List<Value<*>>
                 */
                if (element is Collection<*>) {
                    if (element.firstOrNull() is Value<*>) {
                        element.forEach { checkIfExcluded(list, it as Value<*>) }
                    }
                }

                val superclass = element::class.java.superclass

                /**
                 * For classes with values that include their value-containing super classes
                 *
                 * Example: class ClassWithValues() : OriginalClassWithValues()
                 */
                if (superclass?.`package`?.name?.contains("liquidbounce") == true && !Value::class.java.isAssignableFrom(superclass)) {
                    superclass.declaredFields.forEach {
                        it.isAccessible = true
                        val fieldValue = it[element] ?: return@forEach
                        if (fieldValue is Value<*>) {
                            checkIfExcluded(list, fieldValue)
                        } else {
                            findValues(fieldValue, configurables, list)
                        }
                    }
                }

                element.javaClass.declaredFields.forEach {
                    it.isAccessible = true
                    val fieldValue = it[element] ?: return@forEach

                    if (fieldValue is Value<*>) {
                        checkIfExcluded(list, fieldValue)
                    } else {
                        findValues(fieldValue, configurables, list)
                    }
                }
            } else if (element is Value<*>) {
                checkIfExcluded(list, element)
            } else {
                /**
                 * For variables that hold a list of a possible class that contains Value<*>
                 *
                 * Example: val variable: List<ColorSettingsInt>
                 */
                if (element is Collection<*>) {
                    element.forEach {
                        findValues(it, configurables, list)
                    }
                }
            }

            orderedValues.addAll(list)
        } catch (e: Exception) {
            LOGGER.error(e)
        }
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

}