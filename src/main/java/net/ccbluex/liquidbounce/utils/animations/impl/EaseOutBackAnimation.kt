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
class EaseOutBackAnimation @JvmOverloads constructor(
    duration: Int,
    endPoint: Double,
    private val easeAmount: Float = 1.70158f,
    direction: Direction = Direction.FORWARDS
) : Animation(duration, endPoint, direction) {

    override fun getEquation(x: Double): Double {
        val t = x / duration - 1
        return t * t * ((easeAmount + 1) * t + easeAmount) + 1
    }
}
