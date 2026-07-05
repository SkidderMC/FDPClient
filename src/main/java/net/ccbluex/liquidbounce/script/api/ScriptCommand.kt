/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.script.api

import jdk.nashorn.api.scripting.JSObject
import jdk.nashorn.api.scripting.ScriptUtils
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER

private fun scriptAliases(commandObject: JSObject): Array<String> {
    val converted = ScriptUtils.convert(commandObject.getMember("aliases"), Array<String>::class.java)
    val aliases = converted as? Array<*> ?: error("Script command aliases must be an array.")
    return aliases.mapIndexed { index, alias ->
        alias as? String ?: error("Script command alias at index $index must be a string.")
    }.toTypedArray()
}

@Suppress("SpreadOperator")
class ScriptCommand(private val commandObject: JSObject) : Command(
    commandObject.getMember("name") as String,
    *scriptAliases(commandObject)
) {

    private val events = hashMapOf<String, JSObject>()

    /**
     * Called from inside the script to register a new event handler.
     * @param eventName Name of the event.
     * @param handler JavaScript function used to handle the event.
     */
    fun on(eventName: String, handler: JSObject) {
        events[eventName] = handler
    }

    override fun execute(args: Array<String>) {
        try {
            events["execute"]?.call(commandObject, args)
        } catch (throwable: Throwable) {
            LOGGER.error("[ScriptAPI] Exception in command '$command'!", throwable)
        }
    }
}
