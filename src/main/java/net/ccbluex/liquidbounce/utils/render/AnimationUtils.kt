/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render

import kotlin.math.*

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

    fun doubleAnimate(target: Double, current: Double, speed: Double): Double {
        var current = current
        var speed = speed
        val larger = (target > current)

        if (speed < 0.0) {
            speed = 0.0
        } else if (speed > 1.0) {
            speed = 1.0
        }

        val dif = max(target, current) - min(target, current)
        var factor = dif * speed

        if (factor <= 0.1) factor = 0.1

        if (larger) {
            current += factor
        } else {
            current -= factor
        }

        return current
    }

    fun lstransition(now: Float, desired: Float, speed: Double): Float {
        val dif = abs((desired - now).toDouble())
        val a = abs((desired - (desired - (abs((desired - now).toDouble())))) / (100 - (speed * 10)))
            .toFloat()
        var x = now

        if (dif > 0) {
            if (now < desired) x += a * RenderUtils.deltaTime
            else if (now > desired) x -= a * RenderUtils.deltaTime
        } else x = desired

        if (abs((desired - x).toDouble()) < 10.0E-3 && x != desired) x = desired

        return x
    }
}
