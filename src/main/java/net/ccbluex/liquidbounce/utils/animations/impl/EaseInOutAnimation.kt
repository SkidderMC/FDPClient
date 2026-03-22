/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations.impl

import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction


/**
 * @author Zywl
 */
class EaseInOutAnimation @JvmOverloads constructor(
    duration: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(duration, endPoint, direction) {

    override fun getEquation(x: Double): Double {
        val t = x / duration * 2.0
        return if (t < 1.0) {
            0.5 * t * t
        } else {
            val t2 = t - 1
            -0.5 * (t2 * (t2 - 2) - 1)
        }
    }
}
