/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations.impl

import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction

class SmoothStepAnimation @JvmOverloads constructor(
    duration: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(duration, endPoint, direction) {

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        return x1 * x1 * (3 - 2 * x1)
    }
}
