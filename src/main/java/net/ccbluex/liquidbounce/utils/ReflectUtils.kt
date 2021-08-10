package net.ccbluex.liquidbounce.utils

import org.reflections.Reflections

object ReflectUtils {
    @JvmStatic
    fun <T: Any> getReflects(packagePath: String,clazz: Class<T>): List<Class<out T>> {
        return Reflections(packagePath)
            .getSubTypesOf(clazz)
            .filter { clazz.getDeclaredAnnotation(NotUsable::class.java) == null }
    }
}

annotation class NotUsable