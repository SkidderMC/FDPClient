/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script.api


import jdk.nashorn.api.scripting.JSObject
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Category
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.config.Value
import net.ccbluex.liquidbounce.utils.extensions.toLowerCamelCase

class ScriptModule(name: String, category: Category, description: String, private val moduleObject: JSObject)
    : Module(name, category, forcedDescription = description) {

    private val events = hashMapOf<String, JSObject>()
    private var _tag: String? = null

    /**
     * Allows the user to access values by typing module.settings.<valuename>
     */
    val settings = linkedMapOf<String, Value<*>>()

    private val eventHooks: Map<Class<out Event>, EventHook<in Event>>

    init {
        if (moduleObject.hasMember("settings")) {
            val settings = moduleObject.getMember("settings") as JSObject

            for (settingName in settings.keySet())
                this.settings[settingName] = +(settings.getMember(settingName) as Value<*>)
        }

        if (moduleObject.hasMember("tag"))
            _tag = moduleObject.getMember("tag") as String

        eventHooks = createEventMap { eventClass ->
            val eventName = eventClass.simpleName.removeSuffix("Event").toLowerCamelCase()
            EventHook<Event>(this) { callEvent(eventName, it) }.also {
                EventManager.registerEventHook(eventClass, it)
            }
        }
    }

    override var tag
        get() = _tag
        set(value) {
            _tag = value
        }

    /**
     * Called from inside the script to register a new event handler.
     * @param eventName Name of the event.
     * @param handler JavaScript function used to handle the event.
     */
    fun on(eventName: String, handler: JSObject) {
        events[eventName] = handler
    }

    override fun onEnable() = callEvent("enable", null)

    override fun onDisable() = callEvent("disable", null)

    override fun onUnregister() {
        this.eventHooks.forEach(EventManager::unregisterEventHook)
    }

    /**
     * Calls the handler of a registered event.
     * @param eventName Name of the event to be called.
     * @param payload Event data passed to the handler function.
     */
    private fun callEvent(eventName: String, payload: Any?) {
        try {
            events[eventName]?.call(moduleObject, payload)
        } catch (throwable: Throwable) {
            LOGGER.error("[ScriptAPI] Exception in module '${getName()}'!", throwable)
        }
    }
}