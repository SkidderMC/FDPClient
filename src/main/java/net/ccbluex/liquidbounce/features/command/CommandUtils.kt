/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command

import net.ccbluex.liquidbounce.utils.client.ClientUtils.LOGGER
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance.Companion.mc
import net.ccbluex.liquidbounce.utils.client.chat
import net.minecraft.client.entity.EntityPlayerSP
import net.minecraft.client.multiplayer.WorldClient

/**
 * Crash-safety helpers shared by every [Command].
 *
 * The chat-input hook ([net.ccbluex.liquidbounce.injection.forge.mixins.gui.MixinGuiScreen.messageSend])
 * and [net.ccbluex.liquidbounce.handler.macro.Macro] both call [CommandManager.executeCommands]
 * with NO surrounding try/catch. Anything a command throws therefore propagates through the
 * Minecraft tick/GUI chain and crashes the client. These helpers let commands validate input
 * without ever throwing, and [runSafe] gives a last-line catch-all for the dispatcher.
 */
object CommandUtils {

    /**
     * Run [block] and swallow any [Throwable], reporting it to chat + log instead of letting it
     * bubble up and crash the client. Intended to wrap the body of [CommandManager.executeCommands]
     * (the dispatcher) so a single bad command can never take the game down.
     */
    inline fun runSafe(commandName: String, block: () -> Unit) {
        try {
            block()
        } catch (t: Throwable) {
            LOGGER.error("Command '$commandName' threw an exception", t)
            chat("§cCommand error: §7${t.javaClass.simpleName}: ${t.message}")
        }
    }

    /**
     * Returns args[[index]] or null when out of range. Never throws.
     */
    fun Array<String>.argOrNull(index: Int): String? = getOrNull(index)

    /**
     * Parse args[[index]] as an [Int] within [[min]..[max]] (inclusive).
     * Returns null on missing arg, non-numeric input or out-of-range value — never throws.
     * Use to defend against overflow / runaway-loop input (e.g. `.hurt 999999999`).
     */
    fun Array<String>.intArg(
        index: Int,
        min: Int = Int.MIN_VALUE,
        max: Int = Int.MAX_VALUE
    ): Int? = getOrNull(index)?.toIntOrNull()?.takeIf { it in min..max }

    /**
     * Parse args[[index]] as a [Double] within [[min]..[max]] (inclusive).
     * Returns null on missing arg, non-numeric input, NaN or out-of-range — never throws.
     */
    fun Array<String>.doubleArg(
        index: Int,
        min: Double = -Double.MAX_VALUE,
        max: Double = Double.MAX_VALUE
    ): Double? = getOrNull(index)?.toDoubleOrNull()?.takeIf { !it.isNaN() && it in min..max }

    /**
     * The local player or null. Use instead of `mc.thePlayer` (platform type, implicitly non-null)
     * so commands degrade gracefully when invoked without a world/player.
     */
    fun player(): EntityPlayerSP? = mc.thePlayer

    /**
     * The client world or null. Use instead of bare `mc.theWorld`.
     */
    fun world(): WorldClient? = mc.theWorld

    /**
     * True only when both world and player are present (i.e. actually in-game).
     */
    fun inGame(): Boolean = mc.thePlayer != null && mc.theWorld != null
}
