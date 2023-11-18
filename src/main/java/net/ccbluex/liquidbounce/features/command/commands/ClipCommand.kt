/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.features.command.Command
import kotlin.math.cos
import kotlin.math.sin

class ClipCommand : Command("clip", emptyArray()) {
    override fun execute(args: Array<String>) {
        if (args.size > 2) {
            val dist: Double
            try {
                dist = args[2].toDouble()
            } catch (e: NumberFormatException) {
                chatSyntaxError()
                return
            }

            when (args[1].lowercase()) {
                "up" -> {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + dist, mc.thePlayer.posZ)
                }

                "down" -> {
                    mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY - dist, mc.thePlayer.posZ)
                }

                else -> {
                    val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble() + when (args[1].lowercase()) {
                        "right" -> 90
                        "back" -> 180
                        "left" -> 270
                        else -> 0
                    })
                    val x = -sin(yaw) * dist
                    val z = cos(yaw) * dist
                    mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY, mc.thePlayer.posZ + z)
                }
            }

            return
        }

        chatSyntax("clip <up/down/forward/back/left/right> <dist>")
    }

    override fun tabComplete(args: Array<String>): List<String> {
        if (args.isEmpty()) return emptyList()

        return when (args.size) {
            1 -> listOf("up", "down", "forward", "back", "left", "right").filter { it.startsWith(args[0], true) }

            else -> emptyList()
        }
    }
}