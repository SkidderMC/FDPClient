/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import me.zywl.fdpclient.FDPClient
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.features.module.modules.other.AntiBot
import net.ccbluex.liquidbounce.ui.hud.element.elements.Notification
import net.ccbluex.liquidbounce.ui.hud.element.elements.NotifyType
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.pathfinder.MainPathFinder
import net.ccbluex.liquidbounce.utils.pathfinder.Vec3
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition

class TeleportCommand : Command("tp", arrayOf("teleport")) {

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        if (args.size == 2) {
            val theName = args[1]

            // Get target player data
            val targetPlayer =
                    mc.theWorld.playerEntities.firstOrNull { !AntiBot.isBot(it) && it.name.equals(theName, true) }

            // Attempt to teleport to player's position.
            if (targetPlayer != null) {
                Thread {
                    val path: ArrayList<Vec3> = MainPathFinder.computePath(
                            Vec3(
                                    mc.thePlayer.posX,
                                    mc.thePlayer.posY,
                                    mc.thePlayer.posZ
                            ),
                            Vec3(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ)
                    )
                    for (point in path) PacketUtils.sendPacketNoEvent(
                            C04PacketPlayerPosition(
                                    point.x,
                                    point.y,
                                    point.z,
                                    true
                            )
                    )
                    mc.thePlayer.setPosition(targetPlayer.posX, targetPlayer.posY, targetPlayer.posZ)
                }.start()
                FDPClient.hud.addNotification(
                        Notification(
                                "Successfully teleported to §a${targetPlayer.name}", "Done",
                                NotifyType.SUCCESS
                        )
                )
                return
            } else {
                FDPClient.hud.addNotification(
                        Notification(
                                "No players found!","Error",
                                NotifyType.ERROR
                        )
                )
                return
            }
        } else if (args.size == 4) {
            try {
                val posX = if (args[1].equals("~", true)) mc.thePlayer.posX else args[1].toDouble()
                val posY = if (args[2].equals("~", true)) mc.thePlayer.posY else args[2].toDouble()
                val posZ = if (args[3].equals("~", true)) mc.thePlayer.posZ else args[3].toDouble()
                Thread {
                    val path: ArrayList<Vec3> = MainPathFinder.computePath(
                            Vec3(
                                    mc.thePlayer.posX,
                                    mc.thePlayer.posY,
                                    mc.thePlayer.posZ
                            ),
                            Vec3(posX, posY, posZ)
                    )
                    for (point in path) PacketUtils.sendPacketNoEvent(
                            C04PacketPlayerPosition(
                                    point.x,
                                    point.y,
                                    point.z,
                                    true
                            )
                    )
                    mc.thePlayer.setPosition(posX, posY, posZ)
                }.start()
                FDPClient.hud.addNotification(
                        Notification(
                                "Successfully teleported to §a$posX, $posY, $posZ","Done",
                                NotifyType.SUCCESS
                        )
                )
                return
            } catch (e: NumberFormatException) {
                FDPClient.hud.addNotification(
                        Notification(
                                "Failed to teleport","Error",
                                NotifyType.ERROR
                        )
                )
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