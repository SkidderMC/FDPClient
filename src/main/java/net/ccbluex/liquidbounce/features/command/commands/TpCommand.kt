package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.misc.AntiBot
import net.ccbluex.liquidbounce.utils.PathUtils
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import java.util.function.Consumer
import javax.vecmath.Vector3d

class TpCommand : Command("tp", arrayOf("teleport")) {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            val theName = args[1]

            // Get target player data
            val targetPlayer = mc.theWorld.playerEntities
                .filter { !AntiBot.isBot(it) && it.name.equals(theName, true) }
                .firstOrNull()

            // Attempt to teleport to player's position.
            if (targetPlayer != null) {
                PathUtils.findPath(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ, 4.0)
                    .forEach(Consumer { pos: Vector3d ->
                        mc.netHandler
                            .addToSendQueue(C04PacketPlayerPosition(pos.getX(), pos.getY(), pos.getZ(), true))
                    })
                mc.thePlayer.setPosition(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ)

                return
            } else {
                return
            }
        } else if (args.size == 4) {
            try {
                val posX = if (args[1].equals("~", true)) mc.thePlayer.posX else args[1].toDouble()
                val posY = if (args[2].equals("~", true)) mc.thePlayer.posY else args[2].toDouble()
                val posZ = if (args[3].equals("~", true)) mc.thePlayer.posZ else args[3].toDouble()

                PathUtils.findPath(posX, posY, posZ, 4.0).forEach(Consumer { pos: Vector3d ->
                    mc.netHandler
                        .addToSendQueue(C04PacketPlayerPosition(pos.getX(), pos.getY(), pos.getZ(), true))
                })
                mc.thePlayer.setPosition(posX, posY, posZ)
                return
            } catch (e: NumberFormatException) {

                return
            }
        }

        chatSyntax("tp <player name/x y z>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        val pref = args[0]

        return when (args.size) {
            1 -> mc.theWorld.playerEntities
                .filter { !AntiBot.isBot(it) && it.name.startsWith(pref, true) }
                .map { it.name }
                .toList()

            else -> emptyList()
        }
    }

}