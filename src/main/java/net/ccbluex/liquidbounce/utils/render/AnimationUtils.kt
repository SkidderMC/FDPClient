/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

object AnimationUtils {
    /**
     * In-out-easing function
     * https://github.com/jesusgollonet/processing-penner-easing
     *
     * @param t Current iteration
     * @param d Total iterations
     * @return Eased value
     */
    fun easeOut(t: Float, d: Float) = (t / d - 1).pow(3) + 1

    /**
     * Source: https://easings.net/#easeOutElastic
     *
     * @return A value larger than 0
     */
    fun easeOutElastic(x: Float) =
        when (x) {
            0f, 1f -> x
            else -> 2f.pow(-10 * x) * sin((x * 10 - 0.75f) * (2 * Math.PI / 3f).toFloat()) + 1
        }

    fun animate(target: Float, current: Float, speed: Float): Float {
        var current = current
        var speed = speed
        if (current == target) return current

        val larger = target > current
        if (speed < 0.0f) {
            speed = 0.0f
        } else if (speed > 1.0f) {
            speed = 1.0f
        }

        val dif = max(target.toDouble(), current.toDouble()) - min(target.toDouble(), current.toDouble())
        var factor = dif * speed.toDouble()
        if (factor < 0.1) {
            factor = 0.1
        }

        if (larger) {
            current += factor.toFloat()
            if (current >= target) current = target
        } else if (target < current) {
            current -= factor.toFloat()
            if (current <= target) current = target
        }

        return current
    }
}
