/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations.impl

import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction
import kotlin.math.pow

/**
 * @author Zywl
 */
class EaseInOutQuad @JvmOverloads constructor(
    duration: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(duration, endPoint, direction) {

    override fun getEquation(x1: Double): Double {
        val x = x1 / duration
        return if (x < 0.5) 2 * x.pow(2) else 1 - (-2 * x + 2).pow(2) / 2
    }
}
