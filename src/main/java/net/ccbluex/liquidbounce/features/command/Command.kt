/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.FDPClient.commandManager
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import net.ccbluex.liquidbounce.utils.client.asResourceLocation
import net.ccbluex.liquidbounce.utils.client.playSound

abstract class Command(val command: String, vararg val alias: String) : MinecraftInstance {
    /**
     * Execute commands with provided [args]
     */
    abstract fun execute(args: Array<String>)

    /**
     * Returns a list of command completions based on the provided [args].
     * If a command does not implement [tabComplete] an [EmptyList] is returned by default.
     *
     * @param args an array of command arguments that the player has passed to the command so far
     * @return a list of matching completions for the command the player is trying to autocomplete
     * @author NurMarvin
     */
    open fun tabComplete(args: Array<String>) = emptyList<String>()

    /**
     * Print [msg] to chat
     */
    protected fun chat(msg: String) = net.ccbluex.liquidbounce.utils.client.chat("§3$msg")

    /**
     * Print [alert] to chat as alert
     */
    protected fun alert(msg: String) = chat(msg)

    /**
     * Print [syntax] of command to chat
     */
    protected fun chatSyntax(syntax: String) = chat("§3Syntax: §7${commandManager.prefix}$syntax")

    /**
     * Print [syntaxes] of command to chat
     */
    protected fun chatSyntax(syntaxes: Array<String>) {
        chat("§3Syntax:")

        for (syntax in syntaxes)
            chat("§8> §7${commandManager.prefix}$command ${syntax.lowercase()}")
    }

    /**
     * Print a syntax error to chat
     */
    protected fun chatSyntaxError() = chat("§3Syntax error")

    /**
     * Play edit sound
     */
    protected fun playEdit() {
        synchronized(mc.soundHandler) {
            mc.playSound("random.anvil_use".asResourceLocation())
        }
    }
}