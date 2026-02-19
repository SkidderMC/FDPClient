/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.movement

import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.utils.client.MinecraftInstance
import kotlin.math.sqrt

/**
 * @author itsakc-me
 */
object BPSUtils : MinecraftInstance, Listenable {

    fun getBPS(): Double {
        val player = mc.thePlayer ?: return 0.0

        val deltaX = player.posX - player.prevPosX
        val deltaZ = player.posZ - player.prevPosZ

        return sqrt(deltaX * deltaX + deltaZ * deltaZ)
    }

}
