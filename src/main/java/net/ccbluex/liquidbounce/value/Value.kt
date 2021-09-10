/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/UnlegitMC/FDPClient/
 */
package net.ccbluex.liquidbounce.value

import com.google.gson.JsonElement
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.utils.ClientUtils

abstract class Value<T>(val name: String, protected var value: T) {
    val default=value
    var localedName=""
        get() = if(canLocalized&&field.isNotEmpty()) { field } else { name }

    private var canLocalized=true

    fun cantLocalized():Value<T>{
        canLocalized=false
        return this
    }

    private var displayableFunc: () -> Boolean = { true }

    fun displayable(func: () -> Boolean): Value<T> {
        displayableFunc=func
        return this
    }

    val displayable: Boolean
        get() = displayableFunc()

    fun set(newValue: T) {
        if (newValue == value) return

        val oldValue = get()

        try {
            onChange(oldValue, newValue)
            changeValue(newValue)
            onChanged(oldValue, newValue)
            LiquidBounce.configManager.smartSave()
        } catch (e: Exception) {
            ClientUtils.getLogger().error("[ValueSystem ($name)]: ${e.javaClass.name} (${e.message}) [$oldValue >> $newValue]")
        }
    }

    fun get() = value

    override fun equals(other: Any?): Boolean {
        other ?: return false
        if(value is String && other is String){
            return (value as String).equals(other, true)
        }
        return value?.equals(other) ?: false
    }

    fun setDefault(){
        value=default
    }

    open fun changeValue(value: T) {
        this.value = value
    }

    abstract fun toJson(): JsonElement?
    abstract fun fromJson(element: JsonElement)

    protected open fun onChange(oldValue: T, newValue: T) {}
    protected open fun onChanged(oldValue: T, newValue: T) {}
}