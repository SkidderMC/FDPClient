package net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.nlclickgui.animations

import net.ccbluex.liquidbounce.ui.client.clickgui.style.styles.fdpdropdown.utils.normal.TimerUtil

abstract class Animation @JvmOverloads constructor(
    var duration: Int,
    var endPoint: Double,
    direction: Direction = Direction.FORWARDS
) {

    val timerUtil = TimerUtil()


    var direction: Direction = direction
        set(value) {
            if (field != value) {
                field = value
                timerUtil.setTime(
                    System.currentTimeMillis() - (duration - duration.coerceAtMost(timerUtil.getTime().toInt()).toInt())
                )
            }
        }

    fun finished(direction: Direction): Boolean = isDone() && this.direction == direction

    val linearOutput: Double
        get() = 1 - timerUtil.getTime() / duration.toDouble() * endPoint

    fun reset() {
        timerUtil.reset()
    }

    fun isDone(): Boolean = timerUtil.hasTimeElapsed(duration.toLong())

    fun changeDirection() {

        direction = direction.opposite()
    }




    protected open fun correctOutput(): Boolean = false

    open fun getOutput(): Double {
        return if (direction == Direction.FORWARDS) {
            if (isDone()) endPoint else getEquation(timerUtil.getTime().toDouble()) * endPoint
        } else {
            if (isDone()) {
                0.0
            } else if (correctOutput()) {
                val revTime = duration.coerceAtMost((duration - timerUtil.getTime()).coerceAtLeast(0).toInt()).toDouble()
                getEquation(revTime) * endPoint
            } else {
                (1 - getEquation(timerUtil.getTime().toDouble())) * endPoint
            }
        }
    }

    protected abstract fun getEquation(x: Double): Double
}
