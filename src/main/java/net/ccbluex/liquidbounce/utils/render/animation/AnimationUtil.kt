/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.render.animation

import net.minecraft.client.Minecraft
import kotlin.math.*

object AnimationUtil {
    val debugFPS: Float
        get() = max(Minecraft.getDebugFPS().toFloat(), 60f)

    fun base(current: Double, target: Double, speed: Double): Double {
        return ((current + (target - current) * (speed / (debugFPS / 60.0))) * 1000).toInt() / 1000.0
    }

    fun linear(startTime: Long, duration: Long, start: Double, end: Double): Double {
        return (end - start) * ((System.currentTimeMillis() - startTime) * 1.0 / duration) + start
    }

    fun easeInQuad(startTime: Long, duration: Long, start: Double, end: Double): Double {
        return (end - start) * ((System.currentTimeMillis() - startTime) * 1.0 / duration).pow(2.0) + start
    }

    fun easeOutQuad(startTime: Long, duration: Long, start: Double, end: Double): Double {
        val x = (System.currentTimeMillis() - startTime) * 1.0f / duration
        val y = -x * x + 2 * x
        return start + (end - start) * y
    }

    fun easeInOutQuad(startTime: Long, duration: Long, start: Double, end: Double): Double {
        var t = (System.currentTimeMillis() - startTime) * 1.0f / duration
        t *= 2f
        return if (t < 1) {
            (end - start) / 2 * t * t + start
        } else {
            t--
            -(end - start) / 2 * (t * (t - 2) - 1) + start
        }
    }

    fun easeInOutQuadX(x: Double): Double {
        return if (x < 0.5) { 2 * x * x } else { 1 - (-2 * x + 2).pow(2) / 2 }
    }

    fun easeOutBounce(value: Double): Double {
        val value = value
        val n1 = 7.5625
        val d1 = 2.75
        return when {
            value < 1 / d1 -> n1 * value * value
            value < 2 / d1 -> n1 * (value - 1.5 / d1).let { it * it } + 0.75
            value < 2.5 / d1 -> n1 * (value - 2.25 / d1).let { it * it } + 0.9375
            else -> n1 * (value - 2.625 / d1).let { it * it } + 0.984375
        }
    }

    fun easeInOutElasticx(value: Double): Double {
        val c5 = (2 * Math.PI) / 4.5
        return when {
            value < 0 -> 0.0
            value > 1 -> 1.0
            value < 0.5 -> -(2.0.pow(20 * value - 10) * sin((20 * value - 11.125) * c5)) / 2
            else -> (2.0.pow(-20 * value + 10) * sin((20 * value - 11.125) * c5)) / 2 + 1
        }
    }

    fun easeOutElasticX(value: Double): Double {
        val c4 = (2 * Math.PI) / 3
        return when {
            value < 0 -> 0.0
            value > 1 -> 1.0
            else -> 2.0.pow(-10 * value) * sin((value * 10 - 0.75) * c4) + 1
        }
    }

    fun easeWave(value: Float): Float {
        return (if (value > .5) 1 - value else value) * 2f
    }

    fun easeOutCirc(value: Double): Double {
        return sqrt(1 - (value - 1).pow(2.0))
    }

    fun easeInOutExpo(value: Double): Double {
        return when {
            value < 0 -> 0.0
            value > 1 -> 1.0
            value < 0.5 -> 2.0.pow(20 * value - 10) / 2
            else -> (2 - 2.0.pow(-20 * value + 10)) / 2
        }
    }

    fun easeInElastic(t: Double, b: Double, c: Double, d: Double): Double {
        var t = t
        var s = 1.70158
        var p = 0.0
        var a = c
        if (t == 0.0) return b
        t /= d
        if (t == 1.0) return b + c
        p = d * 0.3
        if (a < abs(c)) {
            a = c
            s = p / 4.0
        } else {
            s = p / (2 * Math.PI) * asin(c / a)
        }
        t--
        return -(a * 2.0.pow(10 * t) * sin((t * d - s) * (2 * Math.PI) / p)) + b
    }

    fun easeOutElastic(t: Double, b: Double, c: Double, d: Double): Double {
        var t = t
        var s = 1.70158
        var p = 0.0
        var a = c
        if (t == 0.0) return b
        t /= d
        if (t == 1.0) return b + c
        p = d * 0.3
        if (a < abs(c)) {
            a = c
            s = p / 4.0
        } else {
            s = p / (2 * Math.PI) * asin(c / a)
        }
        return a * 2.0.pow(-10 * t) * sin((t * d - s) * (2 * Math.PI) / p) + c + b
    }

    fun easeInOutElastic(t: Double, b: Double, c: Double, d: Double): Double {
        var t = t
        var s = 1.70158
        var p = 0.0
        var a = c
        if (t == 0.0) return b
        t /= d / 2
        if (t == 2.0) return b + c
        p = d * (0.3 * 1.5)
        if (a < abs(c)) {
            a = c
            s = p / 4.0
        } else {
            s = p / (2 * Math.PI) * asin(c / a)
        }
        return if (t < 1) {
            t--
            -0.5 * (a * 2.0.pow(10 * t) * sin((t * d - s) * (2 * Math.PI) / p)) + b
        } else {
            t--
            a * 2.0.pow(-10 * t) * sin((t * d - s) * (2 * Math.PI) / p) * 0.5 + c + b
        }
    }

    fun easeInBack(t: Double, b: Double, c: Double, d: Double): Double {
        var t = t
        val s = 1.70158
        t /= d
        return c * t * t * ((s + 1) * t - s) + b
    }

    fun easeInBackNotify(x: Double): Double {
        val c1 = 1.70158;
        val c3 = c1 + 1;

        return c3 * x * x * x - c1 * x * x;
    }


    fun easeOutBackNotify(x: Double): Double {
        val c1 = 1.70158;
        val c3 = c1 + 1;

        return 1 + c3 * (x - 1).pow(3) + c1 * (x - 1).pow(2);
    }

    fun easeOutBack(t: Double, b: Double, c: Double, d: Double): Double {
        var t = t
        val s = 1.70158
        t = t / d - 1
        return c * (t * t * ((s + 1) * t + s) + 1) + b
    }

    fun breathe(duration: Float): Float {
        val progress = System.currentTimeMillis() % duration.toLong() / duration
        return 0.5f * (sin(2 * Math.PI * progress) + 1).toFloat()
    }
}
