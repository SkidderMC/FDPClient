/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils.animations

import net.ccbluex.liquidbounce.utils.timing.MSTimer
import kotlin.math.max
import kotlin.math.min

/**
 * Unified Animation System
 * Replaces all duplicate animation systems across the project
 */
abstract class Animation @JvmOverloads constructor(
    var duration: Int,
    var endPoint: Double,
    direction: Direction = Direction.FORWARDS
) {

    val timerUtil = MSTimer()

    var direction: Direction = direction
        set(newDirection) {
            if (field != newDirection) {
                field = newDirection
                timerUtil.time = System.currentTimeMillis() - (duration.toLong() - min(duration.toLong(), timerUtil.getElapsedTime()))
            }
        }

    val isDone: Boolean
        get() = timerUtil.hasTimeElapsed(duration.toLong())

    val output: Double
        get() = if (direction == Direction.FORWARDS) {
            if (isDone) endPoint
            else getEquation(timerUtil.getElapsedTime().toDouble()) * endPoint
        } else {
            if (isDone) 0.0
            else {
                if (correctOutput()) {
                    val revTime = min(duration.toDouble(), max(0.0, duration.toDouble() - timerUtil.getElapsedTime().toDouble()))
                    getEquation(revTime) * endPoint
                } else {
                    (1 - getEquation(timerUtil.getElapsedTime().toDouble())) * endPoint
                }
            }
        }

    val linearOutput: Double
        get() = 1 - timerUtil.getElapsedTime() / duration.toDouble() * endPoint

    fun setDirection(direction: Direction): Animation {
        this.direction = direction
        return this
    }

    fun finished(direction: Direction): Boolean = isDone && this.direction == direction

    fun reset() = timerUtil.reset()

    fun changeDirection() {
        direction = direction.opposite()
    }

    protected open fun correctOutput(): Boolean = false

    protected abstract fun getEquation(x: Double): Double
}
