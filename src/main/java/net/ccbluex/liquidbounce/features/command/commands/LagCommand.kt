/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class LagCommand : Command("lag", emptyArray()) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            var f = 1
            var time = 0L
            try {
                time = args[1].toLong()
            } catch (e: NumberFormatException) {
                chatSyntaxError()
                return
            }
            var startTime = System.currentTimeMillis()
            while (System.currentTimeMillis() < startTime + time) {
              f = 1 - f
            }
            
            chat("Lagged")
            return
        }

        chatSyntax("lag <milliseconds>")
    }
}
