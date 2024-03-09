/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.misc.StringUtils

class UnicodeCommand : Command("unicode", arrayOf("uc")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size > 1) {
            val message = StringUtils.toCompleteString(args, 1)
            val stringBuilder = StringBuilder()

            for (c in message.toCharArray())
                if (c.code in 33..128)
                    stringBuilder.append(Character.toChars(c.code + 65248)) else stringBuilder.append(c)

            mc.thePlayer.sendChatMessage(stringBuilder.toString())
            return
        }
        chatSyntax("unicode <message>")
    }
}