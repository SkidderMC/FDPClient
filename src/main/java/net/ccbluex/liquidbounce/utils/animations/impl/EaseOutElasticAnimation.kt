/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations.impl

import net.ccbluex.liquidbounce.utils.animations.Animation
import net.ccbluex.liquidbounce.utils.animations.Direction
import kotlin.math.*

/**
 * @author Zywl
 */

class EaseOutElasticAnimation @JvmOverloads constructor(
    duration: Int,
    endPoint: Double,
    direction: Direction = Direction.FORWARDS
) : Animation(duration, endPoint, direction) {

    override fun getEquation(x: Double): Double {
        if (x == 0.0) return 0.0
        val t = x / duration
        if (t == 1.0) return 1.0

        val p = duration * 0.3
        var s = 1.70158
        val a = 1.0

        if (a < 1.0) {
            s = p / 4.0
        } else {
            s = p / (2 * Math.PI) * asin(1.0 / a)
        }

        return a * 2.0.pow(-10 * t) * sin((t * duration - s) * (2 * Math.PI) / p) + 1.0
    }
}
