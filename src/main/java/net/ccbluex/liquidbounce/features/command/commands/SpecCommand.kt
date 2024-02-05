/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.FDPClient
import net.ccbluex.liquidbounce.features.command.Command

class SpecCommand : Command("spec", arrayOf("sc")) {
    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size < 2) {
            if (mc.renderViewEntity != mc.thePlayer) {
                mc.renderViewEntity = mc.thePlayer
                return
            }
            chatSyntax("spec <username>")
            return
        }

        val targetName = args[1]

        for (entity in mc.theWorld.loadedEntityList) {
            if (targetName == entity.name) {
                mc.renderViewEntity = entity
                chat("§7Now you are watching the §4§l${entity.name}§7.")
                chat("§7Use §4§l${FDPClient.commandManager.prefix}spec §7for back for you.")
                break
            }
        }
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> return mc.theWorld.playerEntities
                .map { it.name }
                .filter { it.startsWith(args[0], true) }
            else -> emptyList()
        }
    }
}