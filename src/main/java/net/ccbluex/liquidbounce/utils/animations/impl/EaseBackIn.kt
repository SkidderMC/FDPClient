/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations.impl

import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction
import kotlin.math.max
import kotlin.math.pow

/**
 * @author Zywl
 */
class EaseBackIn @JvmOverloads constructor(
    duration: Int,
    endPoint: Double,
    private val easeAmount: Float = 1.70158f,
    direction: Direction = Direction.FORWARDS
) : Animation(duration, endPoint, direction) {

    override fun correctOutput(): Boolean = true

    override fun getEquation(x: Double): Double {
        val x1 = x / duration
        val shrink = easeAmount + 1
        return max(0.0, 1 + shrink * (x1 - 1).pow(3) + easeAmount * (x1 - 1).pow(2))
    }
}
