/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.animations

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil
import kotlin.math.max
import kotlin.math.min

abstract class Animation(
    protected var duration: Int,
    var endPoint: Double,
    direction: Direction = Direction.FORWARDS
) {

    val timerUtil = TimerUtil()

    fun setDirection(direction: Direction): Animation {
        if (this.direction !== direction) {
            this.direction = direction
            timerUtil.time = System.currentTimeMillis() - (duration.toLong() - min(duration.toLong(), timerUtil.time))
        }
        return this
    }

    var direction: Direction = direction
        set(newDirection) {
            if (field != newDirection) {
                field = newDirection
                timerUtil.time = System.currentTimeMillis() - (duration.toLong() - min(duration.toLong(), timerUtil.time))
            }
        }

    constructor(duration: Int, endPoint: Double) : this(duration, endPoint, Direction.FORWARDS)

    val isDone: Boolean
        get() = timerUtil.hasTimeElapsed(duration.toLong())

    val output: Double
        get() = if (direction == Direction.FORWARDS) {
            if (isDone) endPoint
            else getEquation(timerUtil.time.toDouble()) * endPoint
        } else {
            if (isDone) 0.0
            else {
                if (correctOutput()) {
                    val revTime = min(duration.toDouble(), max(0.0, duration.toDouble() - timerUtil.time.toDouble()))
                    getEquation(revTime) * endPoint
                } else {
                    (1 - getEquation(timerUtil.time.toDouble())) * endPoint
                }
            }
        }

    fun finished(direction: Direction): Boolean = isDone && this.direction == direction

    fun reset() = timerUtil.reset()

    protected open fun correctOutput(): Boolean = false

    protected abstract fun getEquation(x: Double): Double
}
