package net.ccbluex.liquidbounce.ui.ultralight.support

import com.labymedia.ultralight.databind.Databind
import com.labymedia.ultralight.databind.context.ContextProvider
import com.labymedia.ultralight.javascript.JavascriptObject
import net.ccbluex.liquidbounce.utils.misc.MiscUtils

class JSBridge(private val databind: Databind, private val contextProvider: ContextProvider) {
    /**
     * make @param clazz to Any can bypass js engine type check
     */
    fun instanceOf(clazz: Any, obj: Any): Boolean {
        return if (clazz is Class<*>) {
            clazz.isInstance(obj)
        } else {
            false
        }
    }

    fun equal(obj1: Any, obj2: Any): Boolean {
        return obj1.equals(obj2)
    }

    fun forEach(list: List<Any>, func: JavascriptObject) {
        list.forEach { value ->
            try {
                contextProvider.syncWithJavascript {
                    func.callAsFunction(it.context.globalObject, databind.conversionUtils.toJavascript(it.context, value))
                }
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    fun openURL(url: String) {
        MiscUtils.showURL(url)
    }
}